package io.github.joelmomo.runeboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import io.github.joelmomo.runeboard.controller.BindableAction;
import io.github.joelmomo.runeboard.controller.ControllerBindings;
import io.github.joelmomo.runeboard.controller.ControllerKeyNames;
import io.github.joelmomo.runeboard.settings.RunePreferences;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;
import io.github.joelmomo.runeboard.theme.RuneThemes;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MainActivity extends Activity {

    private static final int COLOR_WINDOW = 0xFF0D0E14;
    private static final int COLOR_SURFACE = 0xFF17171F;
    private static final int COLOR_SURFACE_ALT = 0xFF20202B;
    private static final int COLOR_BORDER = 0xFF30303D;
    private static final int COLOR_TEXT = 0xFFF8F8FC;
    private static final int COLOR_MUTED = 0xFF9FA0B4;
    private static final int COLOR_ACCENT = 0xFFA78BFA;

    private final Map<String, LinearLayout> themeCards =
            new LinkedHashMap<>();
    private final Map<Integer, TextView> opacityChips =
            new LinkedHashMap<>();
    private final Map<BindableAction, TextView> bindingChips =
            new LinkedHashMap<>();

    private RunePreferences preferences;
    private BindableAction pendingBinding;
    private TextView bindingStatus;
    private OnBackInvokedCallback backCallback;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new RunePreferences(this);
        backCallback = () -> {
            if (pendingBinding != null) {
                pendingBinding = null;
                refreshBindingControls();
            } else {
                finish();
            }
        };
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                backCallback);

        getWindow().setStatusBarColor(COLOR_WINDOW);
        getWindow().setNavigationBarColor(COLOR_WINDOW);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(COLOR_WINDOW);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(28), dp(24), dp(28), dp(32));

        addHeader(root);

        addSectionHeader(
                root,
                R.string.section_setup,
                R.string.section_setup_subtitle);
        addSetupCards(root);

        addSectionHeader(
                root,
                R.string.section_appearance,
                R.string.section_appearance_subtitle);
        addThemeCards(root);
        addOpacityControl(root);

        addSectionHeader(
                root,
                R.string.section_controls,
                R.string.section_controls_subtitle);
        addControllerBindings(root);

        addSectionHeader(
                root,
                R.string.section_test,
                R.string.section_test_subtitle);
        addTestField(root);
        addSupportLink(root);

        scroll.addView(
                root,
                new ScrollView.LayoutParams(
                        ScrollView.LayoutParams.MATCH_PARENT,
                        ScrollView.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        refreshAppearanceControls();
        refreshBindingControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences != null) {
            refreshAppearanceControls();
            refreshBindingControls();
        }
    }

    @Override
    protected void onDestroy() {
        if (backCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(
                    backCallback);
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (pendingBinding != null) {
            int keyCode = event.getKeyCode();

            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                if (ControllerBindings.isBindableKeyCode(keyCode)) {
                    preferences.setControllerBinding(
                            pendingBinding,
                            keyCode);
                    pendingBinding = null;
                    refreshBindingControls();
                    RuneBoardImeService.requestAppearanceRefresh();
                    return true;
                }
            }

            if (event.getAction() == KeyEvent.ACTION_UP
                    && ControllerBindings.isBindableKeyCode(keyCode)) {
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void addHeader(LinearLayout root) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(this);
        badge.setText("R");
        badge.setTextColor(COLOR_WINDOW);
        badge.setTextSize(23f);
        badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(rounded(COLOR_ACCENT, COLOR_ACCENT, 0, 13f));

        LinearLayout.LayoutParams badgeParams =
                new LinearLayout.LayoutParams(dp(54), dp(54));
        header.addView(badge, badgeParams);

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(14), 0, 0, 0);

        TextView title = text(
                getString(R.string.app_name),
                27f,
                COLOR_TEXT,
                true);
        words.addView(title);

        TextView subtitle = text(
                getString(R.string.settings_subtitle),
                13f,
                COLOR_MUTED,
                false);
        LinearLayout.LayoutParams subtitleParams = wrap();
        subtitleParams.topMargin = dp(2);
        words.addView(subtitle, subtitleParams);

        header.addView(
                words,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f));

        TextView prototype = text(
                getString(R.string.prototype_badge),
                11f,
                COLOR_ACCENT,
                true);
        prototype.setGravity(Gravity.CENTER);
        prototype.setPadding(dp(11), dp(7), dp(11), dp(7));
        prototype.setBackground(
                rounded(0xFF1D1829, 0xFF49386A, 1, 99f));
        header.addView(prototype);

        root.addView(header, matchWidth());

        View divider = new View(this);
        divider.setBackgroundColor(0xFF2A2339);
        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1));
        dividerParams.topMargin = dp(20);
        dividerParams.bottomMargin = dp(2);
        root.addView(divider, dividerParams);
    }

    private void addSectionHeader(
            LinearLayout root,
            int titleRes,
            int subtitleRes) {
        TextView title = text(
                getString(titleRes),
                11f,
                COLOR_ACCENT,
                true);
        title.setLetterSpacing(0.12f);

        LinearLayout.LayoutParams titleParams = matchWidth();
        titleParams.topMargin = dp(22);
        root.addView(title, titleParams);

        TextView subtitle = text(
                getString(subtitleRes),
                13f,
                COLOR_MUTED,
                false);
        LinearLayout.LayoutParams subtitleParams = matchWidth();
        subtitleParams.topMargin = dp(4);
        subtitleParams.bottomMargin = dp(12);
        root.addView(subtitle, subtitleParams);
    }

    private void addSetupCards(LinearLayout root) {
        LinearLayout row = horizontalRow();

        addWeighted(
                row,
                actionCard(
                        R.string.enable_keyboard_title,
                        R.string.enable_keyboard_subtitle,
                        v -> startActivity(
                                new Intent(
                                        Settings.ACTION_INPUT_METHOD_SETTINGS))),
                true);

        addWeighted(
                row,
                actionCard(
                        R.string.choose_keyboard_title,
                        R.string.choose_keyboard_subtitle,
                        v -> {
                            InputMethodManager imm =
                                    getSystemService(InputMethodManager.class);
                            if (imm != null) {
                                imm.showInputMethodPicker();
                            }
                        }),
                false);

        addWeighted(
                row,
                actionCard(
                        R.string.physical_controls_title,
                        R.string.physical_controls_subtitle,
                        v -> startActivity(
                                new Intent(
                                        Settings.ACTION_ACCESSIBILITY_SETTINGS))),
                false);

        root.addView(row, matchWidth());
    }

    private LinearLayout actionCard(
            int titleRes,
            int subtitleRes,
            View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(14), dp(15), dp(14));
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(
                rounded(COLOR_SURFACE, COLOR_BORDER, 1, 12f));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(listener);

        TextView title = text(
                getString(titleRes),
                14f,
                COLOR_TEXT,
                true);
        card.addView(title);

        TextView subtitle = text(
                getString(subtitleRes),
                11f,
                COLOR_MUTED,
                false);
        LinearLayout.LayoutParams subtitleParams = wrap();
        subtitleParams.topMargin = dp(5);
        card.addView(subtitle, subtitleParams);

        card.setMinimumHeight(dp(92));
        return card;
    }

    private void addThemeCards(LinearLayout root) {
        LinearLayout row = horizontalRow();

        addThemeCard(
                row,
                RuneThemes.defaultTheme(),
                R.string.theme_default_title,
                R.string.theme_default_subtitle,
                true);
        addThemeCard(
                row,
                RuneThemes.oledTheme(),
                R.string.theme_oled_title,
                R.string.theme_oled_subtitle,
                false);
        addThemeCard(
                row,
                RuneThemes.transparentTheme(),
                R.string.theme_transparent_title,
                R.string.theme_transparent_subtitle,
                false);

        root.addView(row, matchWidth());
    }

    private void addThemeCard(
            LinearLayout row,
            KeyboardTheme theme,
            int titleRes,
            int subtitleRes,
            boolean first) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setClickable(true);
        card.setFocusable(true);
        card.setContentDescription(
                getString(titleRes) + ". " + getString(subtitleRes));
        card.setOnClickListener(v -> selectTheme(theme.id));

        View swatch = new View(this);
        GradientDrawable swatchDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {
                        theme.backgroundTop,
                        theme.backgroundBottom,
                        theme.selectedFill
                });
        swatchDrawable.setCornerRadius(dp(8));
        swatch.setBackground(swatchDrawable);
        card.addView(
                swatch,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(28)));

        TextView title = text(
                getString(titleRes),
                14f,
                COLOR_TEXT,
                true);
        LinearLayout.LayoutParams titleParams = wrap();
        titleParams.topMargin = dp(10);
        card.addView(title, titleParams);

        TextView subtitle = text(
                getString(subtitleRes),
                11f,
                COLOR_MUTED,
                false);
        LinearLayout.LayoutParams subtitleParams = wrap();
        subtitleParams.topMargin = dp(3);
        card.addView(subtitle, subtitleParams);

        themeCards.put(theme.id, card);
        addWeighted(row, card, first);
    }

    private void addOpacityControl(LinearLayout root) {
        TextView title = text(
                getString(R.string.background_title),
                14f,
                COLOR_TEXT,
                true);
        LinearLayout.LayoutParams titleParams = matchWidth();
        titleParams.topMargin = dp(18);
        root.addView(title, titleParams);

        TextView subtitle = text(
                getString(R.string.background_subtitle),
                11f,
                COLOR_MUTED,
                false);
        LinearLayout.LayoutParams subtitleParams = matchWidth();
        subtitleParams.topMargin = dp(3);
        subtitleParams.bottomMargin = dp(10);
        root.addView(subtitle, subtitleParams);

        LinearLayout row = horizontalRow();
        addOpacityChip(row, 255, "100%", true);
        addOpacityChip(row, 180, "71%", false);
        addOpacityChip(row, 90, "35%", false);
        addOpacityChip(row, 0, "0%", false);
        root.addView(row, matchWidth());
    }

    private void addOpacityChip(
            LinearLayout row,
            int opacity,
            String label,
            boolean first) {
        TextView chip = text(label, 13f, COLOR_TEXT, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(8), dp(10), dp(8), dp(10));
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setOnClickListener(v -> selectOpacity(opacity));

        opacityChips.put(opacity, chip);
        addWeighted(row, chip, first);
    }

    private void addControllerBindings(LinearLayout root) {
        addBindingPair(root, BindableAction.CONFIRM, BindableAction.BACKSPACE);
        addBindingPair(root, BindableAction.SPACE, BindableAction.SHIFT);
        addBindingPair(root, BindableAction.CURSOR_LEFT, BindableAction.CURSOR_RIGHT);
        addBindingPair(root, BindableAction.WORD_LEFT, BindableAction.WORD_RIGHT);
        addBindingPair(root, BindableAction.ENTER, BindableAction.MINIMIZE);

        bindingStatus = text(
                getString(R.string.bindings_ready),
                11f,
                COLOR_MUTED,
                false);
        LinearLayout.LayoutParams statusParams = matchWidth();
        statusParams.topMargin = dp(10);
        root.addView(bindingStatus, statusParams);

        TextView reset = text(
                getString(R.string.bindings_reset),
                12f,
                COLOR_ACCENT,
                true);
        reset.setGravity(Gravity.CENTER);
        reset.setPadding(dp(12), dp(10), dp(12), dp(10));
        reset.setClickable(true);
        reset.setFocusable(true);
        reset.setBackground(
                rounded(COLOR_SURFACE, 0xFF49386A, 1, 10f));
        reset.setOnClickListener(v -> {
            preferences.resetControllerBindings();
            pendingBinding = null;
            refreshBindingControls();
            RuneBoardImeService.requestAppearanceRefresh();
        });

        LinearLayout.LayoutParams resetParams = matchWidth();
        resetParams.topMargin = dp(10);
        root.addView(reset, resetParams);

        TextView fixed = text(
                getString(R.string.bindings_dpad_fixed),
                11f,
                COLOR_MUTED,
                false);
        LinearLayout.LayoutParams fixedParams = matchWidth();
        fixedParams.topMargin = dp(8);
        root.addView(fixed, fixedParams);
    }

    private void addBindingPair(
            LinearLayout root,
            BindableAction first,
            BindableAction second) {
        LinearLayout row = horizontalRow();
        addWeighted(row, bindingCard(first), true);
        addWeighted(row, bindingCard(second), false);

        LinearLayout.LayoutParams rowParams = matchWidth();
        rowParams.bottomMargin = dp(8);
        root.addView(row, rowParams);
    }

    private LinearLayout bindingCard(BindableAction action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(13), dp(11), dp(11), dp(11));
        card.setBackground(
                rounded(COLOR_SURFACE, COLOR_BORDER, 1, 10f));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> {
            pendingBinding = action;
            refreshBindingControls();
        });

        TextView label = text(
                getString(bindingTitle(action)),
                13f,
                COLOR_TEXT,
                true);
        card.addView(
                label,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f));

        TextView chip = text("", 11f, COLOR_ACCENT, true);
        chip.setGravity(Gravity.CENTER);
        chip.setMinWidth(dp(68));
        chip.setPadding(dp(9), dp(7), dp(9), dp(7));
        card.addView(chip);
        bindingChips.put(action, chip);

        return card;
    }

    private int bindingTitle(BindableAction action) {
        switch (action) {
            case CONFIRM:
                return R.string.binding_confirm;
            case BACKSPACE:
                return R.string.binding_backspace;
            case SPACE:
                return R.string.binding_space;
            case SHIFT:
                return R.string.binding_shift;
            case CURSOR_LEFT:
                return R.string.binding_cursor_left;
            case CURSOR_RIGHT:
                return R.string.binding_cursor_right;
            case WORD_LEFT:
                return R.string.binding_word_left;
            case WORD_RIGHT:
                return R.string.binding_word_right;
            case ENTER:
                return R.string.binding_enter;
            case MINIMIZE:
                return R.string.binding_minimize;
            default:
                throw new IllegalArgumentException(
                        "Unknown binding action: " + action);
        }
    }

    private void refreshBindingControls() {
        if (preferences == null) {
            return;
        }

        ControllerBindings bindings = preferences.getControllerBindings();
        for (Map.Entry<BindableAction, TextView> entry
                : bindingChips.entrySet()) {
            BindableAction action = entry.getKey();
            TextView chip = entry.getValue();
            boolean waiting = action == pendingBinding;

            chip.setText(
                    waiting
                            ? getString(R.string.binding_press)
                            : ControllerKeyNames.nameFor(
                                    bindings.getKeyCode(action)));
            chip.setTextColor(waiting ? COLOR_WINDOW : COLOR_ACCENT);
            chip.setBackground(
                    rounded(
                            waiting ? COLOR_ACCENT : COLOR_SURFACE_ALT,
                            waiting ? COLOR_ACCENT : COLOR_BORDER,
                            1,
                            8f));
        }

        if (bindingStatus != null) {
            if (pendingBinding == null) {
                bindingStatus.setText(R.string.bindings_ready);
                bindingStatus.setTextColor(COLOR_MUTED);
            } else {
                bindingStatus.setText(
                        getString(
                                R.string.bindings_waiting,
                                getString(bindingTitle(pendingBinding))));
                bindingStatus.setTextColor(COLOR_ACCENT);
            }
        }
    }

    private void addTestField(LinearLayout root) {
        EditText testField = new EditText(this);
        testField.setHint(R.string.test_hint);
        testField.setTextColor(COLOR_TEXT);
        testField.setHintTextColor(COLOR_MUTED);
        testField.setTextSize(15f);
        testField.setSingleLine(false);
        testField.setMinLines(3);
        testField.setGravity(Gravity.TOP);
        testField.setPadding(dp(15), dp(14), dp(15), dp(14));
        testField.setBackground(
                rounded(COLOR_SURFACE, COLOR_BORDER, 1, 12f));

        LinearLayout.LayoutParams params = matchWidth();
        params.bottomMargin = dp(18);
        root.addView(testField, params);
    }

    private void addSupportLink(LinearLayout root) {
        TextView support = text(
                getString(R.string.more_apps_support),
                12f,
                COLOR_ACCENT,
                true);
        support.setGravity(Gravity.CENTER);
        support.setPadding(dp(12), dp(11), dp(12), dp(11));
        support.setClickable(true);
        support.setFocusable(true);
        support.setBackground(
                rounded(COLOR_SURFACE, 0xFF49386A, 1, 10f));
        support.setOnClickListener(v ->
                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://joelmomo.github.io/#support"))));
        LinearLayout.LayoutParams params = matchWidth();
        params.topMargin = dp(4);
        params.bottomMargin = dp(8);
        root.addView(support, params);
    }

    private void selectTheme(String themeId) {
        preferences.setThemeId(themeId);
        preferences.resetBackgroundOpacity();
        refreshAppearanceControls();
        RuneBoardImeService.requestAppearanceRefresh();
    }

    private void selectOpacity(int opacity) {
        preferences.setBackgroundOpacity(opacity);
        refreshAppearanceControls();
        RuneBoardImeService.requestAppearanceRefresh();
    }

    private void refreshAppearanceControls() {
        String selectedTheme = preferences.getThemeId();
        for (Map.Entry<String, LinearLayout> entry : themeCards.entrySet()) {
            KeyboardTheme theme = RuneThemes.byId(entry.getKey());
            boolean selected = entry.getKey().equals(selectedTheme);
            entry.getValue().setBackground(
                    rounded(
                            selected ? 0xFF201A2D : COLOR_SURFACE,
                            selected ? theme.accent : COLOR_BORDER,
                            selected ? 2 : 1,
                            12f));
        }

        KeyboardTheme activeTheme = preferences.getTheme();
        int activeOpacity = preferences.getBackgroundOpacity(activeTheme);
        for (Map.Entry<Integer, TextView> entry : opacityChips.entrySet()) {
            boolean selected = entry.getKey() == activeOpacity;
            TextView chip = entry.getValue();
            chip.setTextColor(selected ? 0xFF0D0E14 : COLOR_TEXT);
            chip.setBackground(
                    rounded(
                            selected ? activeTheme.accent : COLOR_SURFACE_ALT,
                            selected ? activeTheme.accent : COLOR_BORDER,
                            1,
                            10f));
        }
    }

    private LinearLayout horizontalRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private void addWeighted(
            LinearLayout row,
            View view,
            boolean first) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f);
        if (!first) {
            params.setMarginStart(dp(9));
        }
        row.addView(view, params);
    }

    private TextView text(
            String value,
            float sizeSp,
            int color,
            boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private GradientDrawable rounded(
            int fill,
            int stroke,
            int strokeDp,
            float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), stroke);
        }
        return drawable;
    }

    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(float value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density);
    }
}
