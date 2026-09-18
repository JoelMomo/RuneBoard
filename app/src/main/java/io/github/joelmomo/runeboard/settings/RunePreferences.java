package io.github.joelmomo.runeboard.settings;

import android.content.Context;
import android.content.SharedPreferences;

import io.github.joelmomo.runeboard.theme.BackgroundOpacity;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;
import io.github.joelmomo.runeboard.theme.RuneThemes;

public final class RunePreferences {

    private static final String PREFS_NAME = "runeboard_preferences";
    private static final String KEY_THEME_ID = "theme_id";
    private static final String KEY_BACKGROUND_OPACITY = "background_opacity";

    private final SharedPreferences preferences;

    public RunePreferences(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public KeyboardTheme getTheme() {
        return RuneThemes.byId(
                preferences.getString(KEY_THEME_ID, RuneThemes.ID_DEFAULT));
    }

    public String getThemeId() {
        return getTheme().id;
    }

    public void setThemeId(String id) {
        preferences.edit()
                .putString(KEY_THEME_ID, RuneThemes.byId(id).id)
                .apply();
    }

    public int getBackgroundOpacity(KeyboardTheme theme) {
        if (!preferences.contains(KEY_BACKGROUND_OPACITY)) {
            return theme.defaultBackgroundOpacity;
        }
        return BackgroundOpacity.normalize(
                preferences.getInt(
                        KEY_BACKGROUND_OPACITY,
                        theme.defaultBackgroundOpacity));
    }

    public void setBackgroundOpacity(int opacity) {
        preferences.edit()
                .putInt(
                        KEY_BACKGROUND_OPACITY,
                        BackgroundOpacity.normalize(opacity))
                .apply();
    }

    public void resetBackgroundOpacity() {
        preferences.edit().remove(KEY_BACKGROUND_OPACITY).apply();
    }
}
