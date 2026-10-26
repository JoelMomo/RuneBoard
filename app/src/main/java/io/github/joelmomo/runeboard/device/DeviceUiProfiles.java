package io.github.joelmomo.runeboard.device;

import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.InputDevice;

/** Reads the current device's traits and resolves the stored UI profile preference. */
public final class DeviceUiProfiles {

  private DeviceUiProfiles() {}

  /**
   * @param context the IME service when available, so its display can be checked; any other
   *     context (such as the settings screen) only contributes the static device traits.
   */
  public static UiProfile resolve(Context context, UiProfile preference) {
    if (preference == UiProfile.THOR || preference == UiProfile.MOBILE) {
      return preference;
    }
    return UiProfileResolver.resolve(preference, currentDevice(context));
  }

  public static UiProfileResolver.DeviceTraits currentDevice(Context context) {
    return new UiProfileResolver.DeviceTraits(
        Build.MANUFACTURER,
        Build.BRAND,
        Build.MODEL,
        Build.DISPLAY,
        hasBuiltInGamepad(),
        isOnSecondaryDisplay(context));
  }

  /**
   * A gamepad that is part of the device (not a Bluetooth/USB pad the user plugged in) is the
   * signature of a gaming handheld. External pads are ignored so a phone with a controller paired
   * stays a phone.
   */
  private static boolean hasBuiltInGamepad() {
    for (int id : InputDevice.getDeviceIds()) {
      InputDevice device = InputDevice.getDevice(id);
      if (device == null || device.isVirtual() || device.isExternal()) {
        continue;
      }
      int sources = device.getSources();
      if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
        return true;
      }
    }
    return false;
  }

  private static boolean isOnSecondaryDisplay(Context context) {
    if (context == null) {
      return false;
    }
    try {
      Display display = context.getDisplay();
      return display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY;
    } catch (UnsupportedOperationException notAUiContext) {
      return false;
    }
  }
}
