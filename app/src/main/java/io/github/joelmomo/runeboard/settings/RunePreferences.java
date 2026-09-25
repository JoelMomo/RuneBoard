package io.github.joelmomo.runeboard.settings;

import android.content.Context;
import android.content.SharedPreferences;
import io.github.joelmomo.runeboard.controller.BindableAction;
import io.github.joelmomo.runeboard.controller.ControllerBindings;
import io.github.joelmomo.runeboard.language.KeyboardProfile;
import io.github.joelmomo.runeboard.language.KeyboardProfiles;
import io.github.joelmomo.runeboard.theme.BackgroundOpacity;
import io.github.joelmomo.runeboard.theme.CustomThemeConfig;
import io.github.joelmomo.runeboard.theme.KeyboardFonts;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;
import io.github.joelmomo.runeboard.theme.RuneThemes;
import java.util.EnumMap;

public final class RunePreferences {

  private static final String PREFS_NAME = "runeboard_preferences";
  private static final String KEY_THEME_ID = "theme_id";
  private static final String KEY_PROFILE_ID = "profile_id";
  private static final String KEY_BACKGROUND_OPACITY = "background_opacity";
  private static final String KEY_SUGGESTIONS_ENABLED = "suggestions_enabled";
  private static final String KEY_AUTOCORRECT_ENABLED = "autocorrect_enabled";
  private static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
  private static final String KEY_SOUND_FEEDBACK = "sound_feedback";
  private static final String KEY_FONT_ID = "font_id";
  private static final String KEY_KEY_TEXT_COLOR = "key_text_color";
  private static final String KEY_CUSTOM_ACCENT = "custom_accent";
  private static final String KEY_CUSTOM_KEY_FILL = "custom_key_fill";
  private static final String KEY_CUSTOM_BACKGROUND_TOP = "custom_background_top";
  private static final String KEY_CUSTOM_BACKGROUND_BOTTOM = "custom_background_bottom";
  private static final String KEY_CUSTOM_RADIUS = "custom_key_radius";
  private static final String KEY_CUSTOM_GAP = "custom_key_gap";
  private static final String KEY_BINDING_PREFIX = "binding_";

  private final SharedPreferences preferences;

  public RunePreferences(Context context) {
    preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
  }

  public KeyboardProfile getKeyboardProfile() {
    return KeyboardProfiles.byId(
        preferences.getString(KEY_PROFILE_ID, KeyboardProfiles.defaultProfile().id));
  }

  public String getKeyboardProfileId() {
    return getKeyboardProfile().id;
  }

  public void setKeyboardProfileId(String id) {
    preferences.edit().putString(KEY_PROFILE_ID, KeyboardProfiles.byId(id).id).apply();
  }

  public KeyboardProfile cycleKeyboardProfile() {
    KeyboardProfile next = KeyboardProfiles.next(getKeyboardProfileId());
    setKeyboardProfileId(next.id);
    return next;
  }

  public KeyboardTheme getTheme() {
    String id = getThemeId();
    if (RuneThemes.ID_CUSTOM.equals(id)) {
      return RuneThemes.customTheme(getCustomThemeConfig());
    }
    return RuneThemes.byId(id);
  }

  public String getThemeId() {
    return RuneThemes.normalizeId(preferences.getString(KEY_THEME_ID, RuneThemes.ID_DEFAULT));
  }

  public void setThemeId(String id) {
    preferences.edit().putString(KEY_THEME_ID, RuneThemes.normalizeId(id)).apply();
  }

  public CustomThemeConfig getCustomThemeConfig() {
    CustomThemeConfig defaults = CustomThemeConfig.defaults();
    return new CustomThemeConfig(
        preferences.getInt(KEY_CUSTOM_ACCENT, defaults.accent),
        preferences.getInt(KEY_CUSTOM_KEY_FILL, defaults.keyFill),
        preferences.getInt(KEY_CUSTOM_BACKGROUND_TOP, defaults.backgroundTop),
        preferences.getInt(KEY_CUSTOM_BACKGROUND_BOTTOM, defaults.backgroundBottom),
        preferences.getFloat(KEY_CUSTOM_RADIUS, defaults.keyRadiusDp),
        preferences.getFloat(KEY_CUSTOM_GAP, defaults.keyGapDp));
  }

  public void setCustomAccent(int color) {
    preferences.edit().putInt(KEY_CUSTOM_ACCENT, color).apply();
  }

  public void setCustomKeyFill(int color) {
    preferences.edit().putInt(KEY_CUSTOM_KEY_FILL, color).apply();
  }

  public void setCustomBackground(int top, int bottom) {
    preferences
        .edit()
        .putInt(KEY_CUSTOM_BACKGROUND_TOP, top)
        .putInt(KEY_CUSTOM_BACKGROUND_BOTTOM, bottom)
        .apply();
  }

  public void setCustomRadius(float radiusDp) {
    CustomThemeConfig current = getCustomThemeConfig();
    CustomThemeConfig normalized =
        new CustomThemeConfig(
            current.accent,
            current.keyFill,
            current.backgroundTop,
            current.backgroundBottom,
            radiusDp,
            current.keyGapDp);
    preferences.edit().putFloat(KEY_CUSTOM_RADIUS, normalized.keyRadiusDp).apply();
  }

  public void setCustomGap(float gapDp) {
    CustomThemeConfig current = getCustomThemeConfig();
    CustomThemeConfig normalized =
        new CustomThemeConfig(
            current.accent,
            current.keyFill,
            current.backgroundTop,
            current.backgroundBottom,
            current.keyRadiusDp,
            gapDp);
    preferences.edit().putFloat(KEY_CUSTOM_GAP, normalized.keyGapDp).apply();
  }

  public void resetCustomTheme() {
    preferences
        .edit()
        .remove(KEY_CUSTOM_ACCENT)
        .remove(KEY_CUSTOM_KEY_FILL)
        .remove(KEY_CUSTOM_BACKGROUND_TOP)
        .remove(KEY_CUSTOM_BACKGROUND_BOTTOM)
        .remove(KEY_CUSTOM_RADIUS)
        .remove(KEY_CUSTOM_GAP)
        .apply();
  }

  public int getBackgroundOpacity(KeyboardTheme theme) {
    if (!preferences.contains(KEY_BACKGROUND_OPACITY)) {
      return theme.defaultBackgroundOpacity;
    }
    return BackgroundOpacity.normalize(
        preferences.getInt(KEY_BACKGROUND_OPACITY, theme.defaultBackgroundOpacity));
  }

  public void setBackgroundOpacity(int opacity) {
    preferences.edit().putInt(KEY_BACKGROUND_OPACITY, BackgroundOpacity.normalize(opacity)).apply();
  }

  public void resetBackgroundOpacity() {
    preferences.edit().remove(KEY_BACKGROUND_OPACITY).apply();
  }

  public boolean areSuggestionsEnabled() {
    return preferences.getBoolean(KEY_SUGGESTIONS_ENABLED, true);
  }

  public void setSuggestionsEnabled(boolean enabled) {
    preferences.edit().putBoolean(KEY_SUGGESTIONS_ENABLED, enabled).apply();
  }

  public boolean isAutocorrectEnabled() {
    return preferences.getBoolean(KEY_AUTOCORRECT_ENABLED, true);
  }

  public void setAutocorrectEnabled(boolean enabled) {
    preferences.edit().putBoolean(KEY_AUTOCORRECT_ENABLED, enabled).apply();
  }

  public boolean isHapticFeedbackEnabled() {
    return preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true);
  }

  public void setHapticFeedbackEnabled(boolean enabled) {
    preferences.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply();
  }

  public boolean isSoundFeedbackEnabled() {
    return preferences.getBoolean(KEY_SOUND_FEEDBACK, true);
  }

  public void setSoundFeedbackEnabled(boolean enabled) {
    preferences.edit().putBoolean(KEY_SOUND_FEEDBACK, enabled).apply();
  }

  public String getKeyboardFontId() {
    return KeyboardFonts.normalizeId(preferences.getString(KEY_FONT_ID, KeyboardFonts.ID_SYSTEM));
  }

  public void setKeyboardFontId(String id) {
    preferences.edit().putString(KEY_FONT_ID, KeyboardFonts.normalizeId(id)).apply();
  }

  public int getKeyTextColor(KeyboardTheme theme) {
    if (!preferences.contains(KEY_KEY_TEXT_COLOR)) {
      return theme.textPrimary;
    }
    return preferences.getInt(KEY_KEY_TEXT_COLOR, theme.textPrimary);
  }

  public void setKeyTextColor(int color) {
    preferences.edit().putInt(KEY_KEY_TEXT_COLOR, color).apply();
  }

  public void resetKeyTextColor() {
    preferences.edit().remove(KEY_KEY_TEXT_COLOR).apply();
  }

  public boolean hasKeyTextColorOverride() {
    return preferences.contains(KEY_KEY_TEXT_COLOR);
  }

  public ControllerBindings getControllerBindings() {
    EnumMap<BindableAction, Integer> values = new EnumMap<>(BindableAction.class);
    for (BindableAction action : BindableAction.values()) {
      int keyCode = preferences.getInt(bindingKey(action), action.defaultKeyCode);
      values.put(action, keyCode);
    }
    return new ControllerBindings(values);
  }

  public void setControllerBinding(BindableAction action, int keyCode) {
    ControllerBindings bindings = getControllerBindings();
    bindings.assign(action, keyCode);
    persistBindings(bindings);
  }

  public void resetControllerBindings() {
    SharedPreferences.Editor editor = preferences.edit();
    for (BindableAction action : BindableAction.values()) {
      editor.remove(bindingKey(action));
    }
    editor.apply();
  }

  private void persistBindings(ControllerBindings bindings) {
    SharedPreferences.Editor editor = preferences.edit();
    for (BindableAction action : BindableAction.values()) {
      editor.putInt(bindingKey(action), bindings.getKeyCode(action));
    }
    editor.apply();
  }

  private String bindingKey(BindableAction action) {
    return KEY_BINDING_PREFIX + action.preferenceKey;
  }
}
