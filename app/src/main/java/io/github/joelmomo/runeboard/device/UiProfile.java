package io.github.joelmomo.runeboard.device;

/**
 * Which keyboard presentation RuneBoard uses. {@link #AUTO} is only a preference value; it is
 * always resolved to {@link #THOR} or {@link #MOBILE} before the keyboard is built.
 */
public enum UiProfile {
  AUTO("auto"),
  THOR("thor"),
  MOBILE("mobile");

  public final String preferenceValue;

  UiProfile(String preferenceValue) {
    this.preferenceValue = preferenceValue;
  }

  public static UiProfile fromPreferenceValue(String value) {
    if (value != null) {
      for (UiProfile profile : values()) {
        if (profile.preferenceValue.equals(value)) {
          return profile;
        }
      }
    }
    return AUTO;
  }
}
