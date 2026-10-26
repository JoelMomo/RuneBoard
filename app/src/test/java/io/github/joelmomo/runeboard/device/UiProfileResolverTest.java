package io.github.joelmomo.runeboard.device;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class UiProfileResolverTest {

  private static UiProfileResolver.DeviceTraits phone() {
    return new UiProfileResolver.DeviceTraits(
        "Nothing", "Nothing", "A063", "Pong-U2.6-250101", false, false);
  }

  private static UiProfileResolver.DeviceTraits thor() {
    return new UiProfileResolver.DeviceTraits(
        "AYN", "AYN", "AYN Thor", "Thor_V1.0.0.377_20260206_165408_user", true, true);
  }

  @Test
  public void manualChoicesAlwaysWin() {
    assertEquals(UiProfile.THOR, UiProfileResolver.resolve(UiProfile.THOR, phone()));
    assertEquals(UiProfile.MOBILE, UiProfileResolver.resolve(UiProfile.MOBILE, thor()));
  }

  @Test
  public void autoResolvesPlainPhoneToMobile() {
    assertEquals(UiProfile.MOBILE, UiProfileResolver.resolve(UiProfile.AUTO, phone()));
  }

  @Test
  public void autoKeepsThorOnAnySingleHandheldSignal() {
    assertEquals(UiProfile.THOR, UiProfileResolver.resolve(UiProfile.AUTO, thor()));
    assertEquals(UiProfile.THOR, resolveAuto("ayn", "", "", "", false, false));
    assertEquals(UiProfile.THOR, resolveAuto("", " AYN ", "", "", false, false));
    assertEquals(UiProfile.THOR, resolveAuto("", "", "Thor", "", false, false));
    assertEquals(UiProfile.THOR, resolveAuto("", "", "", "Thor_V1.0.0.377", false, false));
    assertEquals(UiProfile.THOR, resolveAuto("Acme", "Acme", "X1", "X1-1.0", true, false));
    assertEquals(UiProfile.THOR, resolveAuto("Acme", "Acme", "X1", "X1-1.0", false, true));
  }

  @Test
  public void autoDoesNotMatchNamesThatOnlyContainThor() {
    assertEquals(
        UiProfile.MOBILE, resolveAuto("Acme", "Acme", "Mjolnir-thor", "build", false, false));
  }

  @Test
  public void autoTreatsMissingTraitsAsPhone() {
    assertEquals(UiProfile.MOBILE, UiProfileResolver.resolve(UiProfile.AUTO, null));
    assertEquals(UiProfile.MOBILE, resolveAuto(null, null, null, null, false, false));
  }

  @Test
  public void preferenceValuesRoundTripAndUnknownFallsBackToAuto() {
    for (UiProfile profile : UiProfile.values()) {
      assertEquals(profile, UiProfile.fromPreferenceValue(profile.preferenceValue));
    }
    assertEquals(UiProfile.AUTO, UiProfile.fromPreferenceValue(null));
    assertEquals(UiProfile.AUTO, UiProfile.fromPreferenceValue("tablet"));
  }

  private static UiProfile resolveAuto(
      String manufacturer,
      String brand,
      String model,
      String buildDisplay,
      boolean builtInGamepad,
      boolean secondaryDisplay) {
    return UiProfileResolver.resolve(
        UiProfile.AUTO,
        new UiProfileResolver.DeviceTraits(
            manufacturer, brand, model, buildDisplay, builtInGamepad, secondaryDisplay));
  }
}
