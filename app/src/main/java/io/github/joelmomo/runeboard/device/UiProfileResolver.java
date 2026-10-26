package io.github.joelmomo.runeboard.device;

import java.util.Locale;

/**
 * Turns the stored preference into the profile the keyboard actually uses.
 *
 * <p>AUTO is deliberately conservative: it only looks at what the device is, never at its current
 * orientation or window width, so rotating a phone or resizing a window can never flip a Thor into
 * the phone layout. Any single handheld signal is enough to stay on THOR; only a device with none
 * of them becomes MOBILE. The manual THOR/MOBILE choices always win.
 */
public final class UiProfileResolver {

  private UiProfileResolver() {}

  public static UiProfile resolve(UiProfile preference, DeviceTraits device) {
    if (preference == UiProfile.THOR || preference == UiProfile.MOBILE) {
      return preference;
    }
    return looksLikeHandheld(device) ? UiProfile.THOR : UiProfile.MOBILE;
  }

  static boolean looksLikeHandheld(DeviceTraits device) {
    if (device == null) {
      return false;
    }
    if (isAyn(device.manufacturer) || isAyn(device.brand)) {
      return true;
    }
    if (mentionsThor(device.model) || mentionsThor(device.buildDisplay)) {
      return true;
    }
    // The Thor firmware pins the IME to the lower screen, so Android reports it on a
    // secondary display; a phone's IME lives on the default display.
    return device.hasBuiltInGamepad || device.imeOnSecondaryDisplay;
  }

  private static boolean isAyn(String value) {
    return normalize(value).equals("ayn");
  }

  private static boolean mentionsThor(String value) {
    String normalized = normalize(value);
    return normalized.startsWith("ayn") || normalized.startsWith("thor");
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  /** The device facts AUTO looks at, gathered by {@link DeviceUiProfiles}. */
  public static final class DeviceTraits {
    public final String manufacturer;
    public final String brand;
    public final String model;
    /** Android's build display id; Thor firmware reports e.g. {@code Thor_V1.0.0.377_...}. */
    public final String buildDisplay;
    public final boolean hasBuiltInGamepad;
    public final boolean imeOnSecondaryDisplay;

    public DeviceTraits(
        String manufacturer,
        String brand,
        String model,
        String buildDisplay,
        boolean hasBuiltInGamepad,
        boolean imeOnSecondaryDisplay) {
      this.manufacturer = manufacturer;
      this.brand = brand;
      this.model = model;
      this.buildDisplay = buildDisplay;
      this.hasBuiltInGamepad = hasBuiltInGamepad;
      this.imeOnSecondaryDisplay = imeOnSecondaryDisplay;
    }
  }
}
