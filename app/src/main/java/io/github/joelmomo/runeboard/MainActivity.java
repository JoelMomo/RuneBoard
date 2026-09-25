package io.github.joelmomo.runeboard;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import io.github.joelmomo.runeboard.controller.BindableAction;
import io.github.joelmomo.runeboard.controller.ControllerBindings;
import io.github.joelmomo.runeboard.controller.ControllerKeyNames;
import io.github.joelmomo.runeboard.language.KeyboardProfile;
import io.github.joelmomo.runeboard.language.KeyboardProfiles;
import io.github.joelmomo.runeboard.settings.RunePreferences;
import io.github.joelmomo.runeboard.theme.CustomThemeConfig;
import io.github.joelmomo.runeboard.theme.KeyboardFonts;
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

  private final Map<String, LinearLayout> themeCards = new LinkedHashMap<>();
  private final Map<String, View> themeSwatches = new LinkedHashMap<>();
  private final Map<Integer, TextView> opacityChips = new LinkedHashMap<>();
  private final Map<Integer, TextView> customAccentChips = new LinkedHashMap<>();
  private final Map<Integer, TextView> customKeyChips = new LinkedHashMap<>();
  private final Map<Integer, TextView> customBackgroundChips = new LinkedHashMap<>();
  private final Map<Integer, Integer> customBackgroundBottoms = new LinkedHashMap<>();
  private final Map<Integer, TextView> customRadiusChips = new LinkedHashMap<>();
  private final Map<Integer, TextView> customGapChips = new LinkedHashMap<>();
  private final Map<String, TextView> fontChips = new LinkedHashMap<>();
  private final Map<Integer, TextView> keyTextColorChips = new LinkedHashMap<>();
  private final Map<String, LinearLayout> profileCards = new LinkedHashMap<>();
  private final Map<BindableAction, TextView> bindingChips = new LinkedHashMap<>();

  private RunePreferences preferences;
  private BindableAction pendingBinding;
  private TextView bindingStatus;
  private TextView suggestionsChip;
  private TextView autocorrectChip;
  private TextView setupKeyboardChip;
  private TextView setupSelectedChip;
  private TextView setupControlsChip;
  private Switch hapticSwitch;
  private Switch soundSwitch;
  private TextView defaultTextColorChip;
  private LinearLayout customThemePanel;
  private View appearancePreviewSwatch;
  private TextView appearancePreviewSummary;
  private TextView appearanceThemeValue;
  private TextView appearanceOpacityValue;
  private TextView appearanceFontValue;
  private TextView appearanceColorValue;
  private OnBackInvokedCallback backCallback;

  @Override
  @SuppressWarnings("deprecation")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    preferences = new RunePreferences(this);
    backCallback =
        () -> {
          if (pendingBinding != null) {
            pendingBinding = null;
            refreshBindingControls();
          } else {
            finish();
          }
        };
    getOnBackInvokedDispatcher()
        .registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, backCallback);

    getWindow().setStatusBarColor(COLOR_WINDOW);
    getWindow().setNavigationBarColor(COLOR_WINDOW);

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setBackgroundColor(COLOR_WINDOW);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(28), dp(24), dp(28), dp(32));

    addHeader(root);

    boolean setupIncomplete =
        !isRuneBoardEnabled() || !isRuneBoardSelected() || !arePhysicalControlsEnabled();

    addCollapsibleSection(
        root,
        R.string.section_setup,
        R.string.section_setup_subtitle,
        setupIncomplete,
        this::addSetupCards);

    addCollapsibleSection(
        root,
        R.string.section_language,
        R.string.section_language_subtitle,
        false,
        this::addLanguageCards);

    addCollapsibleSection(
        root,
        R.string.section_typing_assistance,
        R.string.section_typing_assistance_subtitle,
        false,
        this::addTypingAssistance);

    addCollapsibleSection(
        root,
        R.string.section_feedback,
        R.string.section_feedback_subtitle,
        false,
        this::addFeedbackControls);

    addCollapsibleSection(
        root,
        R.string.section_appearance,
        R.string.section_appearance_subtitle,
        false,
        this::addAppearanceMenu);

    addCollapsibleSection(
        root,
        R.string.section_controls,
        R.string.section_controls_subtitle,
        false,
        this::addControllerBindings);

    addCollapsibleSection(
        root, R.string.section_test, R.string.section_test_subtitle, false, this::addTestField);

    addSupportLink(root);

    scroll.addView(
        root,
        new ScrollView.LayoutParams(
            ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
    setContentView(scroll);

    refreshSetupControls();
    refreshLanguageControls();
    refreshFeedbackControls();
    refreshAppearanceControls();
    refreshBindingControls();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (preferences != null) {
      refreshSetupControls();
      refreshLanguageControls();
      refreshFeedbackControls();
      refreshAppearanceControls();
      refreshBindingControls();
    }
  }

  @Override
  protected void onDestroy() {
    if (backCallback != null) {
      getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backCallback);
    }
    super.onDestroy();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (pendingBinding != null) {
      int keyCode = event.getKeyCode();

      if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
        if (ControllerBindings.isBindableKeyCode(keyCode)) {
          preferences.setControllerBinding(pendingBinding, keyCode);
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

    LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(54), dp(54));
    header.addView(badge, badgeParams);

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    words.setPadding(dp(14), 0, 0, 0);

    TextView title = text(getString(R.string.app_name), 27f, COLOR_TEXT, true);
    words.addView(title);

    TextView subtitle = text(getString(R.string.settings_subtitle), 13f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = wrap();
    subtitleParams.topMargin = dp(2);
    words.addView(subtitle, subtitleParams);

    header.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    root.addView(header, matchWidth());

    View divider = new View(this);
    divider.setBackgroundColor(0xFF2A2339);
    LinearLayout.LayoutParams dividerParams =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
    dividerParams.topMargin = dp(20);
    dividerParams.bottomMargin = dp(2);
    root.addView(divider, dividerParams);
  }

  private interface SectionBuilder {
    void build(LinearLayout root);
  }

  private interface PickerBuilder {
    void build(LinearLayout root, Dialog dialog);
  }

  private void addCollapsibleSection(
      LinearLayout root, int titleRes, int subtitleRes, boolean expanded, SectionBuilder builder) {
    LinearLayout header = new LinearLayout(this);
    header.setOrientation(LinearLayout.HORIZONTAL);
    header.setGravity(Gravity.CENTER_VERTICAL);
    header.setPadding(dp(14), dp(11), dp(12), dp(11));
    header.setClickable(true);
    header.setFocusable(true);
    header.setMinimumHeight(dp(72));

    View rail = new View(this);
    LinearLayout.LayoutParams railParams = new LinearLayout.LayoutParams(dp(4), dp(42));
    railParams.setMarginEnd(dp(12));
    header.addView(rail, railParams);

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);

    TextView title = text(getString(titleRes), 12f, COLOR_TEXT, true);
    title.setLetterSpacing(0.08f);
    words.addView(title, matchWidth());

    TextView subtitle = text(getString(subtitleRes), 11.5f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = matchWidth();
    subtitleParams.topMargin = dp(3);
    words.addView(subtitle, subtitleParams);

    header.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView arrow = text("", 17f, COLOR_ACCENT, true);
    arrow.setGravity(Gravity.CENTER);
    header.addView(arrow, new LinearLayout.LayoutParams(dp(36), dp(36)));

    LinearLayout body = new LinearLayout(this);
    body.setOrientation(LinearLayout.VERTICAL);
    body.setPadding(0, dp(8), 0, 0);
    builder.build(body);
    body.setVisibility(expanded ? View.VISIBLE : View.GONE);
    styleSectionHeader(header, rail, arrow, expanded);

    header.setOnClickListener(
        v -> {
          boolean show = body.getVisibility() != View.VISIBLE;
          body.setVisibility(show ? View.VISIBLE : View.GONE);
          styleSectionHeader(header, rail, arrow, show);
        });

    LinearLayout.LayoutParams headerParams = matchWidth();
    headerParams.topMargin = dp(14);
    root.addView(header, headerParams);
    root.addView(body, matchWidth());
  }

  private void styleSectionHeader(
      LinearLayout header, View rail, TextView arrow, boolean expanded) {
    header.setBackground(
        rounded(
            expanded ? 0xFF1B1926 : COLOR_SURFACE,
            expanded ? 0xFF554371 : COLOR_BORDER,
            1,
            14f));
    rail.setBackground(rounded(expanded ? COLOR_ACCENT : 0xFF514266, 0, 0, 99f));
    arrow.setText(expanded ? "−" : "+");
    arrow.setTextColor(expanded ? COLOR_WINDOW : COLOR_ACCENT);
    arrow.setBackground(
        rounded(
            expanded ? COLOR_ACCENT : COLOR_SURFACE_ALT,
            expanded ? COLOR_ACCENT : COLOR_BORDER,
            1,
            10f));
  }

  private void addSetupCards(LinearLayout root) {
    setupKeyboardChip = setupStatusChip();
    setupSelectedChip = setupStatusChip();
    setupControlsChip = setupStatusChip();

    addSetupCard(
        root,
        1,
        R.string.enable_keyboard_title,
        R.string.enable_keyboard_subtitle,
        setupKeyboardChip,
        v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));

    addSetupCard(
        root,
        2,
        R.string.choose_keyboard_title,
        R.string.choose_keyboard_subtitle,
        setupSelectedChip,
        v -> {
          InputMethodManager imm = getSystemService(InputMethodManager.class);
          if (imm != null) {
            imm.showInputMethodPicker();
          }
        });

    addSetupCard(
        root,
        3,
        R.string.physical_controls_title,
        R.string.physical_controls_subtitle,
        setupControlsChip,
        v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  }

  private void addSetupCard(
      LinearLayout root,
      int step,
      int titleRes,
      int subtitleRes,
      TextView status,
      View.OnClickListener listener) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(14), dp(13), dp(13), dp(13));
    card.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 1, 12f));
    card.setClickable(true);
    card.setFocusable(true);
    card.setOnClickListener(listener);
    card.setMinimumHeight(dp(78));

    TextView stepView = text(String.valueOf(step), 12f, COLOR_ACCENT, true);
    stepView.setGravity(Gravity.CENTER);
    stepView.setBackground(rounded(COLOR_SURFACE_ALT, 0xFF49386A, 1, 99f));
    card.addView(stepView, new LinearLayout.LayoutParams(dp(34), dp(34)));

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    words.setPadding(dp(12), 0, dp(12), 0);

    TextView title = text(getString(titleRes), 14f, COLOR_TEXT, true);
    words.addView(title);

    TextView subtitle = text(getString(subtitleRes), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = wrap();
    subtitleParams.topMargin = dp(4);
    words.addView(subtitle, subtitleParams);

    card.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    card.addView(status);

    LinearLayout.LayoutParams params = matchWidth();
    params.bottomMargin = dp(8);
    root.addView(card, params);
  }

  private TextView setupStatusChip() {
    TextView chip = text("", 10f, COLOR_ACCENT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setMinWidth(dp(68));
    chip.setPadding(dp(9), dp(7), dp(9), dp(7));
    return chip;
  }

  private void refreshSetupControls() {
    styleSetupStatus(setupKeyboardChip, isRuneBoardEnabled());
    styleSetupStatus(setupSelectedChip, isRuneBoardSelected());
    styleSetupStatus(setupControlsChip, arePhysicalControlsEnabled());
  }

  private void styleSetupStatus(TextView chip, boolean ready) {
    if (chip == null) {
      return;
    }
    chip.setText(ready ? R.string.setup_ready : R.string.setup_required);
    chip.setTextColor(ready ? COLOR_WINDOW : COLOR_ACCENT);
    chip.setBackground(
        rounded(
            ready ? COLOR_ACCENT : COLOR_SURFACE_ALT, ready ? COLOR_ACCENT : COLOR_BORDER, 1, 99f));
  }

  private boolean isRuneBoardEnabled() {
    InputMethodManager imm = getSystemService(InputMethodManager.class);
    if (imm == null) {
      return false;
    }

    ComponentName target = new ComponentName(this, RuneBoardImeService.class);
    for (InputMethodInfo info : imm.getEnabledInputMethodList()) {
      if (target.equals(info.getComponent())) {
        return true;
      }
    }
    return false;
  }

  private boolean isRuneBoardSelected() {
    String current =
        Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
    if (current == null) {
      return false;
    }

    ComponentName target = new ComponentName(this, RuneBoardImeService.class);
    return current.equals(target.flattenToShortString())
        || current.equals(target.flattenToString());
  }

  private boolean arePhysicalControlsEnabled() {
    AccessibilityManager manager = getSystemService(AccessibilityManager.class);
    if (manager == null) {
      return false;
    }

    ComponentName target = new ComponentName(this, RuneBoardControlService.class);
    for (AccessibilityServiceInfo info :
        manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
      if (info.getResolveInfo() == null || info.getResolveInfo().serviceInfo == null) {
        continue;
      }
      ComponentName enabled =
          new ComponentName(
              info.getResolveInfo().serviceInfo.packageName,
              info.getResolveInfo().serviceInfo.name);
      if (target.equals(enabled)) {
        return true;
      }
    }
    return false;
  }

  private void addLanguageCards(LinearLayout root) {
    java.util.List<KeyboardProfile> profiles = KeyboardProfiles.builtIns();

    for (int index = 0; index < profiles.size(); index += 2) {
      LinearLayout row = horizontalRow();
      addWeighted(row, languageCard(profiles.get(index)), true);

      if (index + 1 < profiles.size()) {
        addWeighted(row, languageCard(profiles.get(index + 1)), false);
      }

      LinearLayout.LayoutParams rowParams = matchWidth();
      rowParams.bottomMargin = dp(9);
      root.addView(row, rowParams);
    }
  }

  private LinearLayout languageCard(KeyboardProfile profile) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(14), dp(12), dp(14), dp(12));
    card.setClickable(true);
    card.setFocusable(true);
    card.setContentDescription(profile.displayName + ". " + profile.layoutName);
    card.setOnClickListener(
        v -> {
          preferences.setKeyboardProfileId(profile.id);
          refreshLanguageControls();
          RuneBoardImeService.requestAppearanceRefresh();
        });

    TextView title = text(profile.displayName, 14f, COLOR_TEXT, true);
    card.addView(title);

    TextView subtitle =
        text(profile.shortLabel + " / " + profile.layoutName, 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = wrap();
    subtitleParams.topMargin = dp(4);
    card.addView(subtitle, subtitleParams);

    profileCards.put(profile.id, card);
    return card;
  }

  private void refreshLanguageControls() {
    if (preferences == null) {
      return;
    }

    String activeId = preferences.getKeyboardProfileId();
    for (Map.Entry<String, LinearLayout> entry : profileCards.entrySet()) {
      boolean selected = entry.getKey().equals(activeId);
      entry
          .getValue()
          .setBackground(
              rounded(
                  selected ? 0xFF201A2D : COLOR_SURFACE,
                  selected ? COLOR_ACCENT : COLOR_BORDER,
                  selected ? 2 : 1,
                  12f));
    }
  }

  private void addTypingAssistance(LinearLayout root) {
    LinearLayout row = horizontalRow();
    addWeighted(row, typingAssistanceCard(true), true);
    addWeighted(row, typingAssistanceCard(false), false);
    root.addView(row, matchWidth());
  }

  private LinearLayout typingAssistanceCard(boolean suggestions) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(14), dp(12), dp(12), dp(12));
    card.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 1, 12f));
    card.setClickable(true);
    card.setFocusable(true);

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    TextView title =
        text(
            getString(suggestions ? R.string.suggestions_title : R.string.autocorrect_title),
            14f,
            COLOR_TEXT,
            true);
    words.addView(title);
    TextView subtitle =
        text(
            getString(suggestions ? R.string.suggestions_subtitle : R.string.autocorrect_subtitle),
            11f,
            COLOR_MUTED,
            false);
    LinearLayout.LayoutParams subtitleParams = wrap();
    subtitleParams.topMargin = dp(4);
    words.addView(subtitle, subtitleParams);
    card.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView chip = text("", 11f, COLOR_ACCENT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setMinWidth(dp(52));
    chip.setPadding(dp(8), dp(7), dp(8), dp(7));
    card.addView(chip);
    if (suggestions) {
      suggestionsChip = chip;
      card.setOnClickListener(
          v -> {
            boolean enabled = !preferences.areSuggestionsEnabled();
            preferences.setSuggestionsEnabled(enabled);
            if (!enabled) {
              preferences.setAutocorrectEnabled(false);
            }
            refreshTypingAssistance();
            RuneBoardImeService.requestAppearanceRefresh();
          });
    } else {
      autocorrectChip = chip;
      card.setOnClickListener(
          v -> {
            boolean enabled = !preferences.isAutocorrectEnabled();
            preferences.setAutocorrectEnabled(enabled);
            if (enabled) {
              preferences.setSuggestionsEnabled(true);
            }
            refreshTypingAssistance();
            RuneBoardImeService.requestAppearanceRefresh();
          });
    }
    card.setMinimumHeight(dp(86));
    return card;
  }

  private void refreshTypingAssistance() {
    if (preferences == null) {
      return;
    }
    styleToggleChip(suggestionsChip, preferences.areSuggestionsEnabled());
    styleToggleChip(autocorrectChip, preferences.isAutocorrectEnabled());
  }

  private void styleToggleChip(TextView chip, boolean enabled) {
    if (chip == null) {
      return;
    }
    chip.setText(enabled ? R.string.toggle_on : R.string.toggle_off);
    chip.setTextColor(enabled ? COLOR_WINDOW : COLOR_MUTED);
    chip.setBackground(
        rounded(
            enabled ? COLOR_ACCENT : COLOR_SURFACE_ALT,
            enabled ? COLOR_ACCENT : COLOR_BORDER,
            1,
            9f));
  }

  private void addFeedbackControls(LinearLayout root) {
    LinearLayout row = horizontalRow();
    addWeighted(row, feedbackCard(true), true);
    addWeighted(row, feedbackCard(false), false);
    root.addView(row, matchWidth());
  }

  private LinearLayout feedbackCard(boolean haptic) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(14), dp(12), dp(12), dp(12));
    card.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 1, 12f));
    card.setClickable(true);
    card.setFocusable(true);

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    TextView title =
        text(
            getString(haptic ? R.string.haptic_feedback_title : R.string.sound_feedback_title),
            14f,
            COLOR_TEXT,
            true);
    words.addView(title);
    TextView subtitle =
        text(
            getString(
                haptic ? R.string.haptic_feedback_subtitle : R.string.sound_feedback_subtitle),
            11f,
            COLOR_MUTED,
            false);
    LinearLayout.LayoutParams subtitleParams = wrap();
    subtitleParams.topMargin = dp(4);
    words.addView(subtitle, subtitleParams);
    card.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    Switch toggle = new Switch(this);
    toggle.setShowText(false);
    toggle.setChecked(
        haptic ? preferences.isHapticFeedbackEnabled() : preferences.isSoundFeedbackEnabled());
    card.addView(toggle);
    if (haptic) {
      hapticSwitch = toggle;
      toggle.setOnCheckedChangeListener(
          (button, enabled) -> {
            preferences.setHapticFeedbackEnabled(enabled);
            RuneBoardImeService.requestAppearanceRefresh();
          });
    } else {
      soundSwitch = toggle;
      toggle.setOnCheckedChangeListener(
          (button, enabled) -> {
            preferences.setSoundFeedbackEnabled(enabled);
            RuneBoardImeService.requestAppearanceRefresh();
          });
    }
    card.setOnClickListener(v -> toggle.setChecked(!toggle.isChecked()));
    card.setMinimumHeight(dp(86));
    return card;
  }

  private void refreshFeedbackControls() {
    if (hapticSwitch != null) {
      hapticSwitch.setChecked(preferences.isHapticFeedbackEnabled());
    }
    if (soundSwitch != null) {
      soundSwitch.setChecked(preferences.isSoundFeedbackEnabled());
    }
  }

  private void addAppearanceMenu(LinearLayout root) {
    LinearLayout preview = new LinearLayout(this);
    preview.setOrientation(LinearLayout.HORIZONTAL);
    preview.setGravity(Gravity.CENTER_VERTICAL);
    preview.setPadding(dp(14), dp(13), dp(14), dp(13));
    preview.setBackground(rounded(0xFF14131C, 0xFF3A3150, 1, 14f));

    appearancePreviewSwatch = new View(this);
    preview.addView(appearancePreviewSwatch, new LinearLayout.LayoutParams(dp(86), dp(46)));

    LinearLayout previewWords = new LinearLayout(this);
    previewWords.setOrientation(LinearLayout.VERTICAL);
    previewWords.setPadding(dp(14), 0, 0, 0);

    TextView previewTitle =
        text(getString(R.string.appearance_preview_title), 13f, COLOR_TEXT, true);
    previewWords.addView(previewTitle);

    appearancePreviewSummary = text("", 11.5f, COLOR_MUTED, false);
    LinearLayout.LayoutParams previewSummaryParams = matchWidth();
    previewSummaryParams.topMargin = dp(4);
    previewWords.addView(appearancePreviewSummary, previewSummaryParams);

    preview.addView(
        previewWords, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    root.addView(preview, matchWidth());

    addAppearanceGroupLabel(root, R.string.appearance_group_style);
    appearanceThemeValue =
        addAppearanceRow(
            root,
            "◆",
            R.string.appearance_theme_title,
            R.string.appearance_theme_subtitle,
            v -> showThemePicker());
    appearanceOpacityValue =
        addAppearanceRow(
            root,
            "%",
            R.string.background_title,
            R.string.background_subtitle,
            v -> showOpacityPicker());

    addAppearanceGroupLabel(root, R.string.appearance_group_lettering);
    appearanceFontValue =
        addAppearanceRow(
            root,
            "Aa",
            R.string.appearance_font_title,
            R.string.appearance_font_subtitle,
            v -> showFontPicker());
    appearanceColorValue =
        addAppearanceRow(
            root,
            "●",
            R.string.appearance_color_title,
            R.string.appearance_color_subtitle,
            v -> showColorPicker());

    addAppearanceGroupLabel(root, R.string.appearance_group_advanced);
    addAppearanceRow(
        root,
        "⋯",
        R.string.custom_theme_title,
        R.string.appearance_custom_subtitle,
        v -> showCustomStylePicker());

    refreshAppearanceSummary();
  }

  private void addAppearanceGroupLabel(LinearLayout root, int labelRes) {
    TextView label = text(getString(labelRes), 10f, COLOR_MUTED, true);
    label.setLetterSpacing(0.12f);
    LinearLayout.LayoutParams params = matchWidth();
    params.topMargin = dp(15);
    params.bottomMargin = dp(6);
    params.setMarginStart(dp(2));
    root.addView(label, params);
  }

  private TextView addAppearanceRow(
      LinearLayout root,
      String mark,
      int titleRes,
      int subtitleRes,
      View.OnClickListener listener) {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(12), dp(11), dp(12), dp(11));
    row.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 1, 13f));
    row.setClickable(true);
    row.setFocusable(true);
    row.setMinimumHeight(dp(72));
    row.setOnClickListener(listener);

    TextView marker = text(mark, "Aa".equals(mark) ? 13f : 16f, COLOR_ACCENT, true);
    marker.setGravity(Gravity.CENTER);
    marker.setBackground(rounded(COLOR_SURFACE_ALT, 0xFF443754, 1, 11f));
    row.addView(marker, new LinearLayout.LayoutParams(dp(46), dp(46)));

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    words.setPadding(dp(12), 0, dp(10), 0);

    TextView title = text(getString(titleRes), 13.5f, COLOR_TEXT, true);
    words.addView(title);

    TextView subtitle = text(getString(subtitleRes), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = matchWidth();
    subtitleParams.topMargin = dp(3);
    words.addView(subtitle, subtitleParams);

    row.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView value = text("", 11.5f, COLOR_ACCENT, true);
    value.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
    value.setMaxLines(1);
    row.addView(value, new LinearLayout.LayoutParams(dp(220), dp(44)));

    LinearLayout.LayoutParams rowParams = matchWidth();
    rowParams.bottomMargin = dp(8);
    root.addView(row, rowParams);
    return value;
  }

  private void refreshAppearanceSummary() {
    if (appearancePreviewSummary == null) {
      return;
    }

    KeyboardTheme activeTheme = preferences.getTheme();
    String theme = appearanceThemeName(preferences.getThemeId());
    String font = appearanceFontName(preferences.getKeyboardFontId());
    String opacity = appearanceOpacityName(preferences.getBackgroundOpacity(activeTheme));
    String color = appearanceColorName(activeTheme);

    appearancePreviewSummary.setText(
        getString(R.string.appearance_preview_summary, theme, font, opacity));
    appearanceThemeValue.setText(theme + "  ›");
    appearanceOpacityValue.setText(opacity + "  ›");
    appearanceFontValue.setText(font + "  ›");
    appearanceColorValue.setText(color + "  ›");

    GradientDrawable preview =
        new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[] {
              activeTheme.backgroundTop, activeTheme.backgroundBottom, activeTheme.selectedFill
            });
    preview.setCornerRadius(dp(10));
    preview.setStroke(dp(1), 0xFF514266);
    appearancePreviewSwatch.setBackground(preview);
  }

  private String appearanceThemeName(String themeId) {
    if (RuneThemes.ID_OLED.equals(themeId)) {
      return getString(R.string.theme_oled_title);
    }
    if (RuneThemes.ID_TRANSPARENT.equals(themeId)) {
      return getString(R.string.theme_transparent_title);
    }
    if (RuneThemes.ID_CUSTOM.equals(themeId)) {
      return getString(R.string.theme_custom_title);
    }
    return getString(R.string.theme_default_title);
  }

  private String appearanceFontName(String fontId) {
    if (KeyboardFonts.ID_INTER.equals(fontId)) {
      return getString(R.string.font_inter);
    }
    if (KeyboardFonts.ID_ATKINSON.equals(fontId)) {
      return getString(R.string.font_atkinson);
    }
    if (KeyboardFonts.ID_JETBRAINS_MONO.equals(fontId)) {
      return getString(R.string.font_jetbrains_mono);
    }
    if (KeyboardFonts.ID_SPACE_GROTESK.equals(fontId)) {
      return getString(R.string.font_space_grotesk);
    }
    if (KeyboardFonts.ID_MEDIEVAL_SHARP.equals(fontId)) {
      return getString(R.string.font_medieval_sharp);
    }
    return getString(R.string.font_system);
  }

  private String appearanceOpacityName(int opacity) {
    if (opacity == 170) {
      return "67%";
    }
    if (opacity == 85) {
      return "33%";
    }
    if (opacity == 0) {
      return "0%";
    }
    return "100%";
  }

  private String appearanceColorName(KeyboardTheme theme) {
    if (!preferences.hasKeyTextColorOverride()) {
      return getString(R.string.color_theme);
    }
    int color = preferences.getKeyTextColor(theme);
    if (color == 0xFFF8F8FC) {
      return getString(R.string.color_white);
    }
    if (color == 0xFFC4B5FD) {
      return getString(R.string.color_lavender);
    }
    if (color == 0xFF67E8F9) {
      return getString(R.string.color_cyan);
    }
    if (color == 0xFF86EFAC) {
      return getString(R.string.color_green);
    }
    if (color == 0xFFFDE68A) {
      return getString(R.string.color_amber);
    }
    if (color == 0xFFF9A8D4) {
      return getString(R.string.color_pink);
    }
    return getString(R.string.color_custom);
  }

  private void showThemePicker() {
    showPickerDialog(
        R.string.appearance_theme_title,
        R.string.appearance_theme_subtitle,
        false,
        (root, dialog) -> {
          addThemePickerOption(
              root,
              dialog,
              RuneThemes.defaultTheme(),
              R.string.theme_default_title,
              R.string.theme_default_subtitle);
          addThemePickerOption(
              root,
              dialog,
              RuneThemes.oledTheme(),
              R.string.theme_oled_title,
              R.string.theme_oled_subtitle);
          addThemePickerOption(
              root,
              dialog,
              RuneThemes.transparentTheme(),
              R.string.theme_transparent_title,
              R.string.theme_transparent_subtitle);
          addThemePickerOption(
              root,
              dialog,
              RuneThemes.customTheme(preferences.getCustomThemeConfig()),
              R.string.theme_custom_title,
              R.string.theme_custom_subtitle);
        });
  }

  private void addThemePickerOption(
      LinearLayout root, Dialog dialog, KeyboardTheme theme, int titleRes, int subtitleRes) {
    boolean selected = theme.id.equals(preferences.getThemeId());
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(12), dp(10), dp(12), dp(10));
    row.setBackground(
        rounded(
            selected ? 0xFF211B2F : COLOR_SURFACE_ALT,
            selected ? theme.accent : COLOR_BORDER,
            selected ? 2 : 1,
            12f));
    row.setClickable(true);
    row.setFocusable(true);

    View swatch = new View(this);
    GradientDrawable swatchDrawable =
        new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[] {theme.backgroundTop, theme.backgroundBottom, theme.selectedFill});
    swatchDrawable.setCornerRadius(dp(9));
    swatch.setBackground(swatchDrawable);
    row.addView(swatch, new LinearLayout.LayoutParams(dp(72), dp(42)));

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    words.setPadding(dp(12), 0, dp(8), 0);
    TextView title = text(getString(titleRes), 13.5f, COLOR_TEXT, true);
    words.addView(title);
    TextView subtitle = text(getString(subtitleRes), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = matchWidth();
    subtitleParams.topMargin = dp(3);
    words.addView(subtitle, subtitleParams);
    row.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView check = text(selected ? "✓" : "", 17f, theme.accent, true);
    check.setGravity(Gravity.CENTER);
    row.addView(check, new LinearLayout.LayoutParams(dp(36), dp(36)));

    row.setOnClickListener(
        v -> {
          selectTheme(theme.id);
          dialog.dismiss();
        });

    LinearLayout.LayoutParams params = matchWidth();
    params.bottomMargin = dp(8);
    root.addView(row, params);
  }

  private void showOpacityPicker() {
    showPickerDialog(
        R.string.background_title,
        R.string.background_subtitle,
        false,
        (root, dialog) -> {
          LinearLayout row = horizontalRow();
          addOpacityPickerChip(row, dialog, 255, "100%", true);
          addOpacityPickerChip(row, dialog, 170, "67%", false);
          addOpacityPickerChip(row, dialog, 85, "33%", false);
          addOpacityPickerChip(row, dialog, 0, "0%", false);
          root.addView(row, matchWidth());
        });
  }

  private void addOpacityPickerChip(
      LinearLayout row, Dialog dialog, int opacity, String label, boolean first) {
    int selectedOpacity = preferences.getBackgroundOpacity(preferences.getTheme());
    boolean selected = selectedOpacity == opacity;
    TextView chip = text(label, 13f, selected ? COLOR_WINDOW : COLOR_TEXT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(8), dp(13), dp(8), dp(13));
    chip.setBackground(
        rounded(
            selected ? preferences.getTheme().accent : COLOR_SURFACE_ALT,
            selected ? preferences.getTheme().accent : COLOR_BORDER,
            selected ? 2 : 1,
            11f));
    chip.setClickable(true);
    chip.setFocusable(true);
    chip.setOnClickListener(
        v -> {
          selectOpacity(opacity);
          dialog.dismiss();
        });
    addWeighted(row, chip, first);
  }

  private void showFontPicker() {
    showPickerDialog(
        R.string.appearance_font_title,
        R.string.appearance_font_subtitle,
        false,
        (root, dialog) -> {
          addFontPickerOption(root, dialog, KeyboardFonts.ID_SYSTEM, R.string.font_system);
          addFontPickerOption(root, dialog, KeyboardFonts.ID_INTER, R.string.font_inter);
          addFontPickerOption(root, dialog, KeyboardFonts.ID_ATKINSON, R.string.font_atkinson);
          addFontPickerOption(
              root, dialog, KeyboardFonts.ID_JETBRAINS_MONO, R.string.font_jetbrains_mono);
          addFontPickerOption(
              root, dialog, KeyboardFonts.ID_SPACE_GROTESK, R.string.font_space_grotesk);
          addFontPickerOption(
              root, dialog, KeyboardFonts.ID_MEDIEVAL_SHARP, R.string.font_medieval_sharp);
        });
  }

  private void addFontPickerOption(
      LinearLayout root, Dialog dialog, String fontId, int labelRes) {
    boolean selected = fontId.equals(preferences.getKeyboardFontId());
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(12), dp(9), dp(12), dp(9));
    row.setBackground(
        rounded(
            selected ? 0xFF211B2F : COLOR_SURFACE_ALT,
            selected ? COLOR_ACCENT : COLOR_BORDER,
            selected ? 2 : 1,
            12f));
    row.setClickable(true);
    row.setFocusable(true);

    TextView sample = text("Aa", 18f, COLOR_TEXT, true);
    sample.setGravity(Gravity.CENTER);
    sample.setTypeface(KeyboardFonts.resolve(this, fontId), Typeface.BOLD);
    sample.setBackground(rounded(0xFF14131C, 0xFF40354F, 1, 10f));
    row.addView(sample, new LinearLayout.LayoutParams(dp(58), dp(44)));

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    words.setPadding(dp(12), 0, dp(8), 0);
    TextView title = text(getString(labelRes), 13.5f, COLOR_TEXT, true);
    title.setTypeface(KeyboardFonts.resolve(this, fontId), Typeface.BOLD);
    words.addView(title);
    TextView preview = text("RuneBoard", 11.5f, COLOR_MUTED, false);
    preview.setTypeface(KeyboardFonts.resolve(this, fontId));
    LinearLayout.LayoutParams previewParams = matchWidth();
    previewParams.topMargin = dp(2);
    words.addView(preview, previewParams);
    row.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView check = text(selected ? "✓" : "", 17f, COLOR_ACCENT, true);
    check.setGravity(Gravity.CENTER);
    row.addView(check, new LinearLayout.LayoutParams(dp(36), dp(36)));

    row.setOnClickListener(
        v -> {
          preferences.setKeyboardFontId(fontId);
          refreshAppearanceControls();
          RuneBoardImeService.requestAppearanceRefresh();
          dialog.dismiss();
        });

    LinearLayout.LayoutParams params = matchWidth();
    params.bottomMargin = dp(8);
    root.addView(row, params);
  }

  private void showColorPicker() {
    showPickerDialog(
        R.string.appearance_color_title,
        R.string.appearance_color_subtitle,
        false,
        (root, dialog) -> {
          KeyboardTheme theme = preferences.getTheme();
          addColorPickerOption(root, dialog, theme.textPrimary, R.string.color_theme, true);
          addColorPickerOption(root, dialog, 0xFFF8F8FC, R.string.color_white, false);
          addColorPickerOption(root, dialog, 0xFFC4B5FD, R.string.color_lavender, false);
          addColorPickerOption(root, dialog, 0xFF67E8F9, R.string.color_cyan, false);
          addColorPickerOption(root, dialog, 0xFF86EFAC, R.string.color_green, false);
          addColorPickerOption(root, dialog, 0xFFFDE68A, R.string.color_amber, false);
          addColorPickerOption(root, dialog, 0xFFF9A8D4, R.string.color_pink, false);
        });
  }

  private void addColorPickerOption(
      LinearLayout root, Dialog dialog, int color, int labelRes, boolean themeColor) {
    KeyboardTheme theme = preferences.getTheme();
    boolean defaultColor = !preferences.hasKeyTextColorOverride();
    boolean selected =
        themeColor ? defaultColor : !defaultColor && preferences.getKeyTextColor(theme) == color;

    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(12), dp(9), dp(12), dp(9));
    row.setBackground(
        rounded(
            selected ? 0xFF211B2F : COLOR_SURFACE_ALT,
            selected ? COLOR_ACCENT : COLOR_BORDER,
            selected ? 2 : 1,
            12f));
    row.setClickable(true);
    row.setFocusable(true);

    View swatch = new View(this);
    GradientDrawable dot = new GradientDrawable();
    dot.setShape(GradientDrawable.OVAL);
    dot.setColor(color);
    dot.setStroke(dp(selected ? 2 : 1), selected ? COLOR_ACCENT : 0xFF555565);
    swatch.setBackground(dot);
    LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(dp(34), dp(34));
    swatchParams.setMarginStart(dp(7));
    swatchParams.setMarginEnd(dp(19));
    row.addView(swatch, swatchParams);

    TextView title = text(getString(labelRes), 13.5f, COLOR_TEXT, true);
    row.addView(
        title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView check = text(selected ? "✓" : "", 17f, COLOR_ACCENT, true);
    check.setGravity(Gravity.CENTER);
    row.addView(check, new LinearLayout.LayoutParams(dp(36), dp(36)));

    row.setOnClickListener(
        v -> {
          if (themeColor) {
            preferences.resetKeyTextColor();
          } else {
            preferences.setKeyTextColor(color);
          }
          refreshAppearanceControls();
          RuneBoardImeService.requestAppearanceRefresh();
          dialog.dismiss();
        });

    LinearLayout.LayoutParams params = matchWidth();
    params.bottomMargin = dp(8);
    root.addView(row, params);
  }

  private void showCustomStylePicker() {
    showPickerDialog(
        R.string.custom_theme_title,
        R.string.appearance_custom_subtitle,
        true,
        (root, dialog) -> {
          customAccentChips.clear();
          customKeyChips.clear();
          customBackgroundChips.clear();
          customBackgroundBottoms.clear();
          customRadiusChips.clear();
          customGapChips.clear();
          addCustomThemeControls(root);
          if (customThemePanel != null) {
            customThemePanel.setVisibility(View.VISIBLE);
          }
          refreshCustomThemeControls();
        });
  }

  private void showPickerDialog(
      int titleRes, int subtitleRes, boolean tall, PickerBuilder builder) {
    Dialog dialog = new Dialog(this);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    LinearLayout shell = new LinearLayout(this);
    shell.setOrientation(LinearLayout.VERTICAL);
    shell.setPadding(dp(16), dp(15), dp(16), dp(16));
    shell.setBackground(rounded(COLOR_SURFACE, 0xFF463A5A, 1, 18f));

    LinearLayout header = new LinearLayout(this);
    header.setOrientation(LinearLayout.HORIZONTAL);
    header.setGravity(Gravity.CENTER_VERTICAL);

    LinearLayout words = new LinearLayout(this);
    words.setOrientation(LinearLayout.VERTICAL);
    TextView title = text(getString(titleRes), 18f, COLOR_TEXT, true);
    words.addView(title);
    TextView subtitle = text(getString(subtitleRes), 11.5f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = matchWidth();
    subtitleParams.topMargin = dp(3);
    words.addView(subtitle, subtitleParams);
    header.addView(
        words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    TextView close = text("×", 22f, COLOR_MUTED, false);
    close.setGravity(Gravity.CENTER);
    close.setClickable(true);
    close.setFocusable(true);
    close.setBackground(rounded(COLOR_SURFACE_ALT, COLOR_BORDER, 1, 10f));
    close.setOnClickListener(v -> dialog.dismiss());
    header.addView(close, new LinearLayout.LayoutParams(dp(38), dp(38)));
    shell.addView(header, matchWidth());

    View divider = new View(this);
    divider.setBackgroundColor(0xFF30283F);
    LinearLayout.LayoutParams dividerParams =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
    dividerParams.topMargin = dp(13);
    dividerParams.bottomMargin = dp(12);
    shell.addView(divider, dividerParams);

    ScrollView scroller = new ScrollView(this);
    scroller.setFillViewport(false);
    LinearLayout content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    builder.build(content, dialog);
    scroller.addView(content, matchWidth());

    int contentHeight =
        tall
            ? Math.max(
                dp(260),
                Math.min(dp(620), getResources().getDisplayMetrics().heightPixels - dp(190)))
            : LinearLayout.LayoutParams.WRAP_CONTENT;
    shell.addView(
        scroller,
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, contentHeight));

    dialog.setContentView(shell);
    dialog.setCanceledOnTouchOutside(true);
    dialog.show();

    Window window = dialog.getWindow();
    if (window != null) {
      window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
      window.setDimAmount(0.62f);
      int available = getResources().getDisplayMetrics().widthPixels - dp(40);
      window.setLayout(Math.min(dp(620), available), WindowManager.LayoutParams.WRAP_CONTENT);
    }
  }

  private void addThemeCards(LinearLayout root) {
    LinearLayout firstRow = horizontalRow();
    addThemeCard(
        firstRow,
        RuneThemes.defaultTheme(),
        R.string.theme_default_title,
        R.string.theme_default_subtitle,
        true);
    addThemeCard(
        firstRow,
        RuneThemes.oledTheme(),
        R.string.theme_oled_title,
        R.string.theme_oled_subtitle,
        false);
    root.addView(firstRow, matchWidth());

    LinearLayout secondRow = horizontalRow();
    LinearLayout.LayoutParams secondRowParams = matchWidth();
    secondRowParams.topMargin = dp(9);
    addThemeCard(
        secondRow,
        RuneThemes.transparentTheme(),
        R.string.theme_transparent_title,
        R.string.theme_transparent_subtitle,
        true);
    addThemeCard(
        secondRow,
        RuneThemes.customTheme(preferences.getCustomThemeConfig()),
        R.string.theme_custom_title,
        R.string.theme_custom_subtitle,
        false);
    root.addView(secondRow, secondRowParams);
  }

  private void addThemeCard(
      LinearLayout row, KeyboardTheme theme, int titleRes, int subtitleRes, boolean first) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(12), dp(12), dp(12), dp(12));
    card.setClickable(true);
    card.setFocusable(true);
    card.setContentDescription(getString(titleRes) + ". " + getString(subtitleRes));
    card.setOnClickListener(v -> selectTheme(theme.id));

    View swatch = new View(this);
    GradientDrawable swatchDrawable =
        new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[] {theme.backgroundTop, theme.backgroundBottom, theme.selectedFill});
    swatchDrawable.setCornerRadius(dp(8));
    swatch.setBackground(swatchDrawable);
    card.addView(
        swatch, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(28)));

    TextView title = text(getString(titleRes), 14f, COLOR_TEXT, true);
    LinearLayout.LayoutParams titleParams = wrap();
    titleParams.topMargin = dp(10);
    card.addView(title, titleParams);

    TextView subtitle = text(getString(subtitleRes), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = wrap();
    subtitleParams.topMargin = dp(3);
    card.addView(subtitle, subtitleParams);

    themeCards.put(theme.id, card);
    themeSwatches.put(theme.id, swatch);
    addWeighted(row, card, first);
  }

  private void addOpacityControl(LinearLayout root) {
    TextView title = text(getString(R.string.background_title), 14f, COLOR_TEXT, true);
    LinearLayout.LayoutParams titleParams = matchWidth();
    titleParams.topMargin = dp(18);
    root.addView(title, titleParams);

    TextView subtitle = text(getString(R.string.background_subtitle), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = matchWidth();
    subtitleParams.topMargin = dp(3);
    subtitleParams.bottomMargin = dp(10);
    root.addView(subtitle, subtitleParams);

    LinearLayout row = horizontalRow();
    addOpacityChip(row, 255, "100%", true);
    addOpacityChip(row, 170, "67%", false);
    addOpacityChip(row, 85, "33%", false);
    addOpacityChip(row, 0, "0%", false);
    root.addView(row, matchWidth());
  }

  private void addOpacityChip(LinearLayout row, int opacity, String label, boolean first) {
    TextView chip = text(label, 13f, COLOR_TEXT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(8), dp(10), dp(8), dp(10));
    chip.setClickable(true);
    chip.setFocusable(true);
    chip.setOnClickListener(v -> selectOpacity(opacity));

    opacityChips.put(opacity, chip);
    addWeighted(row, chip, first);
  }

  private void addTypographyControls(LinearLayout root) {
    TextView title = text(getString(R.string.typography_title), 14f, COLOR_TEXT, true);
    LinearLayout.LayoutParams titleParams = matchWidth();
    titleParams.topMargin = dp(18);
    root.addView(title, titleParams);

    TextView subtitle = text(getString(R.string.typography_subtitle), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = matchWidth();
    subtitleParams.topMargin = dp(4);
    subtitleParams.bottomMargin = dp(10);
    root.addView(subtitle, subtitleParams);

    TextView fontLabel = text(getString(R.string.typography_font), 10f, COLOR_MUTED, true);
    root.addView(fontLabel, matchWidth());

    LinearLayout firstFonts = horizontalRow();
    addFontChip(firstFonts, KeyboardFonts.ID_SYSTEM, R.string.font_system, true);
    addFontChip(firstFonts, KeyboardFonts.ID_INTER, R.string.font_inter, false);
    addFontChip(firstFonts, KeyboardFonts.ID_ATKINSON, R.string.font_atkinson, false);
    root.addView(firstFonts, matchWidth());

    LinearLayout secondFonts = horizontalRow();
    addFontChip(secondFonts, KeyboardFonts.ID_JETBRAINS_MONO, R.string.font_jetbrains_mono, true);
    addFontChip(secondFonts, KeyboardFonts.ID_SPACE_GROTESK, R.string.font_space_grotesk, false);
    addFontChip(secondFonts, KeyboardFonts.ID_MEDIEVAL_SHARP, R.string.font_medieval_sharp, false);
    root.addView(secondFonts, matchWidth());

    TextView colorLabel = text(getString(R.string.typography_color), 10f, COLOR_MUTED, true);
    LinearLayout.LayoutParams colorLabelParams = matchWidth();
    colorLabelParams.topMargin = dp(14);
    root.addView(colorLabel, colorLabelParams);

    LinearLayout firstColors = horizontalRow();
    addDefaultTextColorChip(firstColors, true);
    addKeyTextColorChip(firstColors, 0xFFF8F8FC, R.string.color_white, false);
    addKeyTextColorChip(firstColors, 0xFFC4B5FD, R.string.color_lavender, false);
    addKeyTextColorChip(firstColors, 0xFF67E8F9, R.string.color_cyan, false);
    root.addView(firstColors, matchWidth());

    LinearLayout secondColors = horizontalRow();
    addKeyTextColorChip(secondColors, 0xFF86EFAC, R.string.color_green, true);
    addKeyTextColorChip(secondColors, 0xFFFDE68A, R.string.color_amber, false);
    addKeyTextColorChip(secondColors, 0xFFF9A8D4, R.string.color_pink, false);
    root.addView(secondColors, matchWidth());
  }

  private void addFontChip(LinearLayout row, String fontId, int labelRes, boolean first) {
    TextView chip = text(getString(labelRes), 11f, COLOR_TEXT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(8), dp(10), dp(8), dp(10));
    chip.setTypeface(KeyboardFonts.resolve(this, fontId), Typeface.BOLD);
    chip.setClickable(true);
    chip.setFocusable(true);
    chip.setOnClickListener(
        v -> {
          preferences.setKeyboardFontId(fontId);
          refreshTypographyControls();
          RuneBoardImeService.requestAppearanceRefresh();
        });
    fontChips.put(fontId, chip);
    addWeighted(row, chip, first);
  }

  private void addDefaultTextColorChip(LinearLayout row, boolean first) {
    TextView chip = text(getString(R.string.color_theme), 11f, COLOR_TEXT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(8), dp(10), dp(8), dp(10));
    chip.setClickable(true);
    chip.setFocusable(true);
    chip.setOnClickListener(
        v -> {
          preferences.resetKeyTextColor();
          refreshTypographyControls();
          RuneBoardImeService.requestAppearanceRefresh();
        });
    defaultTextColorChip = chip;
    addWeighted(row, chip, first);
  }

  private void addKeyTextColorChip(LinearLayout row, int color, int labelRes, boolean first) {
    TextView chip = text(getString(labelRes), 11f, contrastText(color), true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(8), dp(10), dp(8), dp(10));
    chip.setClickable(true);
    chip.setFocusable(true);
    chip.setOnClickListener(
        v -> {
          preferences.setKeyTextColor(color);
          refreshTypographyControls();
          RuneBoardImeService.requestAppearanceRefresh();
        });
    keyTextColorChips.put(color, chip);
    addWeighted(row, chip, first);
  }

  private void refreshTypographyControls() {
    String selectedFont = preferences.getKeyboardFontId();
    for (Map.Entry<String, TextView> entry : fontChips.entrySet()) {
      boolean selected = entry.getKey().equals(selectedFont);
      entry.getValue().setTextColor(selected ? COLOR_WINDOW : COLOR_TEXT);
      entry
          .getValue()
          .setBackground(
              rounded(
                  selected ? COLOR_ACCENT : COLOR_SURFACE_ALT,
                  selected ? COLOR_ACCENT : COLOR_BORDER,
                  selected ? 2 : 1,
                  9f));
    }

    KeyboardTheme activeTheme = preferences.getTheme();
    boolean defaultColor = !preferences.hasKeyTextColorOverride();
    if (defaultTextColorChip != null) {
      int themeColor = activeTheme.textPrimary;
      defaultTextColorChip.setTextColor(defaultColor ? contrastText(themeColor) : COLOR_MUTED);
      defaultTextColorChip.setBackground(
          rounded(
              defaultColor ? themeColor : COLOR_SURFACE_ALT,
              defaultColor ? COLOR_ACCENT : COLOR_BORDER,
              defaultColor ? 2 : 1,
              9f));
    }

    int selectedColor = preferences.getKeyTextColor(activeTheme);
    for (Map.Entry<Integer, TextView> entry : keyTextColorChips.entrySet()) {
      boolean selected = !defaultColor && entry.getKey() == selectedColor;
      entry.getValue().setTextColor(contrastText(entry.getKey()));
      entry
          .getValue()
          .setBackground(
              rounded(
                  entry.getKey(), selected ? COLOR_ACCENT : COLOR_BORDER, selected ? 2 : 1, 9f));
    }
  }

  private void addCustomThemeControls(LinearLayout root) {
    customThemePanel = new LinearLayout(this);
    customThemePanel.setOrientation(LinearLayout.VERTICAL);
    customThemePanel.setVisibility(View.GONE);
    LinearLayout panel = customThemePanel;
    TextView title = text(getString(R.string.custom_theme_title), 14f, COLOR_TEXT, true);
    LinearLayout.LayoutParams titleParams = matchWidth();
    titleParams.topMargin = dp(18);
    panel.addView(title, titleParams);

    TextView subtitle = text(getString(R.string.custom_theme_subtitle), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams subtitleParams = matchWidth();
    subtitleParams.topMargin = dp(3);
    subtitleParams.bottomMargin = dp(10);
    panel.addView(subtitle, subtitleParams);

    addCustomLabel(panel, R.string.custom_accent);
    LinearLayout accentRow = horizontalRow();
    addCustomColorChip(
        accentRow,
        customAccentChips,
        0xFFA78BFA,
        "Purple",
        true,
        () -> applyCustomAccent(0xFFA78BFA));
    addCustomColorChip(
        accentRow,
        customAccentChips,
        0xFF22D3EE,
        "Cyan",
        false,
        () -> applyCustomAccent(0xFF22D3EE));
    addCustomColorChip(
        accentRow,
        customAccentChips,
        0xFF34D399,
        "Green",
        false,
        () -> applyCustomAccent(0xFF34D399));
    addCustomColorChip(
        accentRow,
        customAccentChips,
        0xFFFBBF24,
        "Amber",
        false,
        () -> applyCustomAccent(0xFFFBBF24));
    addCustomColorChip(
        accentRow,
        customAccentChips,
        0xFFF472B6,
        "Pink",
        false,
        () -> applyCustomAccent(0xFFF472B6));
    panel.addView(accentRow, matchWidth());

    addCustomLabel(panel, R.string.custom_keys);
    LinearLayout keyRow = horizontalRow();
    addCustomColorChip(
        keyRow, customKeyChips, 0xFF2B2B38, "Graphite", true, () -> applyCustomKeyFill(0xFF2B2B38));
    addCustomColorChip(
        keyRow, customKeyChips, 0xFF17171D, "Black", false, () -> applyCustomKeyFill(0xFF17171D));
    addCustomColorChip(
        keyRow, customKeyChips, 0xFF1E293B, "Navy", false, () -> applyCustomKeyFill(0xFF1E293B));
    addCustomColorChip(
        keyRow, customKeyChips, 0xFF3B314D, "Violet", false, () -> applyCustomKeyFill(0xFF3B314D));
    panel.addView(keyRow, matchWidth());

    addCustomLabel(panel, R.string.custom_background);
    LinearLayout backgroundRow = horizontalRow();
    addCustomBackgroundChip(backgroundRow, 0xFF0D0E14, 0xFF181321, "Rune", true);
    addCustomBackgroundChip(backgroundRow, 0xFF000000, 0xFF000000, "Black", false);
    addCustomBackgroundChip(backgroundRow, 0xFF06131F, 0xFF0D2638, "Navy", false);
    addCustomBackgroundChip(backgroundRow, 0xFF130C20, 0xFF25143A, "Violet", false);
    panel.addView(backgroundRow, matchWidth());

    addCustomLabel(panel, R.string.custom_radius);
    LinearLayout radiusRow = horizontalRow();
    addCustomMetricChip(
        radiusRow, customRadiusChips, 4, "Square", true, () -> applyCustomRadius(4f));
    addCustomMetricChip(
        radiusRow, customRadiusChips, 12, "Round", false, () -> applyCustomRadius(12f));
    addCustomMetricChip(
        radiusRow, customRadiusChips, 20, "Soft", false, () -> applyCustomRadius(20f));
    panel.addView(radiusRow, matchWidth());

    addCustomLabel(panel, R.string.custom_gap);
    LinearLayout gapRow = horizontalRow();
    addCustomMetricChip(gapRow, customGapChips, 3, "Tight", true, () -> applyCustomGap(3f));
    addCustomMetricChip(gapRow, customGapChips, 6, "Normal", false, () -> applyCustomGap(6f));
    addCustomMetricChip(gapRow, customGapChips, 9, "Wide", false, () -> applyCustomGap(9f));
    panel.addView(gapRow, matchWidth());

    TextView reset = text(getString(R.string.custom_reset), 12f, COLOR_ACCENT, true);
    reset.setGravity(Gravity.CENTER);
    reset.setPadding(dp(12), dp(10), dp(12), dp(10));
    reset.setClickable(true);
    reset.setFocusable(true);
    reset.setBackground(rounded(COLOR_SURFACE, 0xFF49386A, 1, 10f));
    reset.setOnClickListener(
        v -> {
          preferences.resetCustomTheme();
          activateCustomTheme();
        });
    LinearLayout.LayoutParams resetParams = matchWidth();
    resetParams.topMargin = dp(10);
    panel.addView(reset, resetParams);

    root.addView(panel, matchWidth());
  }

  private void addCustomLabel(LinearLayout root, int textRes) {
    TextView label = text(getString(textRes), 11f, COLOR_MUTED, true);
    LinearLayout.LayoutParams params = matchWidth();
    params.topMargin = dp(12);
    params.bottomMargin = dp(6);
    root.addView(label, params);
  }

  private void addCustomColorChip(
      LinearLayout row,
      Map<Integer, TextView> target,
      int color,
      String label,
      boolean first,
      Runnable action) {
    TextView chip = text(label, 11f, contrastText(color), true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(6), dp(10), dp(6), dp(10));
    chip.setClickable(true);
    chip.setFocusable(true);
    chip.setBackground(rounded(color, color, 1, 9f));
    chip.setOnClickListener(v -> action.run());
    target.put(color, chip);
    addWeighted(row, chip, first);
  }

  private void addCustomBackgroundChip(
      LinearLayout row, int top, int bottom, String label, boolean first) {
    TextView chip = text(label, 11f, contrastText(top), true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(6), dp(10), dp(6), dp(10));
    chip.setClickable(true);
    chip.setFocusable(true);
    GradientDrawable drawable =
        new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {top, bottom});
    drawable.setCornerRadius(dp(9));
    chip.setBackground(drawable);
    chip.setOnClickListener(v -> applyCustomBackground(top, bottom));
    customBackgroundChips.put(top, chip);
    customBackgroundBottoms.put(top, bottom);
    addWeighted(row, chip, first);
  }

  private void addCustomMetricChip(
      LinearLayout row,
      Map<Integer, TextView> target,
      int value,
      String label,
      boolean first,
      Runnable action) {
    TextView chip = text(label, 11f, COLOR_TEXT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(7), dp(10), dp(7), dp(10));
    chip.setClickable(true);
    chip.setFocusable(true);
    chip.setOnClickListener(v -> action.run());
    target.put(value, chip);
    addWeighted(row, chip, first);
  }

  private void applyCustomAccent(int color) {
    preferences.setCustomAccent(color);
    activateCustomTheme();
  }

  private void applyCustomKeyFill(int color) {
    preferences.setCustomKeyFill(color);
    activateCustomTheme();
  }

  private void applyCustomBackground(int top, int bottom) {
    preferences.setCustomBackground(top, bottom);
    activateCustomTheme();
  }

  private void applyCustomRadius(float radiusDp) {
    preferences.setCustomRadius(radiusDp);
    activateCustomTheme();
  }

  private void applyCustomGap(float gapDp) {
    preferences.setCustomGap(gapDp);
    activateCustomTheme();
  }

  private void activateCustomTheme() {
    preferences.setThemeId(RuneThemes.ID_CUSTOM);
    refreshAppearanceControls();
    RuneBoardImeService.requestAppearanceRefresh();
  }

  private int contrastText(int color) {
    int red = (color >>> 16) & 0xFF;
    int green = (color >>> 8) & 0xFF;
    int blue = color & 0xFF;
    int brightness = (red * 299 + green * 587 + blue * 114) / 1000;
    return brightness >= 170 ? COLOR_WINDOW : COLOR_TEXT;
  }

  private void refreshCustomThemeControls() {
    CustomThemeConfig config = preferences.getCustomThemeConfig();
    styleCustomColorMap(customAccentChips, config.accent);
    styleCustomColorMap(customKeyChips, config.keyFill);
    styleCustomColorMap(customBackgroundChips, config.backgroundTop);
    styleCustomMetricMap(customRadiusChips, Math.round(config.keyRadiusDp), config.accent);
    styleCustomMetricMap(customGapChips, Math.round(config.keyGapDp), config.accent);
  }

  private void styleCustomColorMap(Map<Integer, TextView> values, int selectedColor) {
    for (Map.Entry<Integer, TextView> entry : values.entrySet()) {
      int fill = entry.getKey();
      boolean selected = fill == selectedColor;
      TextView chip = entry.getValue();
      chip.setTextColor(contrastText(fill));
      chip.setBackground(rounded(fill, selected ? 0xFFFFFFFF : fill, selected ? 2 : 1, 9f));
    }
  }

  private void styleCustomBackgroundMap(int selectedTop) {
    for (Map.Entry<Integer, TextView> entry : customBackgroundChips.entrySet()) {
      int top = entry.getKey();
      int bottom = customBackgroundBottoms.getOrDefault(top, top);
      boolean selected = top == selectedTop;
      GradientDrawable drawable =
          new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {top, bottom});
      drawable.setCornerRadius(dp(9));
      if (selected) {
        drawable.setStroke(dp(2), 0xFFFFFFFF);
      }
      entry.getValue().setBackground(drawable);
      entry.getValue().setTextColor(contrastText(top));
    }
  }

  private void styleCustomMetricMap(Map<Integer, TextView> values, int selectedValue, int accent) {
    for (Map.Entry<Integer, TextView> entry : values.entrySet()) {
      boolean selected = entry.getKey() == selectedValue;
      TextView chip = entry.getValue();
      chip.setTextColor(selected ? COLOR_WINDOW : COLOR_TEXT);
      chip.setBackground(
          rounded(selected ? accent : COLOR_SURFACE_ALT, selected ? accent : COLOR_BORDER, 1, 9f));
    }
  }

  private void addControllerBindings(LinearLayout root) {
    addBindingPair(root, BindableAction.CONFIRM, BindableAction.BACKSPACE);
    addBindingPair(root, BindableAction.SPACE, BindableAction.SHIFT);
    addBindingPair(root, BindableAction.CURSOR_LEFT, BindableAction.CURSOR_RIGHT);
    addBindingPair(root, BindableAction.WORD_LEFT, BindableAction.WORD_RIGHT);
    addBindingPair(root, BindableAction.ENTER, BindableAction.MINIMIZE);
    addBindingPair(root, BindableAction.LANGUAGE_NEXT, BindableAction.ACCEPT_SUGGESTION);

    bindingStatus = text(getString(R.string.bindings_ready), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams statusParams = matchWidth();
    statusParams.topMargin = dp(10);
    root.addView(bindingStatus, statusParams);

    TextView reset = text(getString(R.string.bindings_reset), 12f, COLOR_ACCENT, true);
    reset.setGravity(Gravity.CENTER);
    reset.setPadding(dp(12), dp(10), dp(12), dp(10));
    reset.setClickable(true);
    reset.setFocusable(true);
    reset.setBackground(rounded(COLOR_SURFACE, 0xFF49386A, 1, 10f));
    reset.setOnClickListener(
        v -> {
          preferences.resetControllerBindings();
          pendingBinding = null;
          refreshBindingControls();
          RuneBoardImeService.requestAppearanceRefresh();
        });

    LinearLayout.LayoutParams resetParams = matchWidth();
    resetParams.topMargin = dp(10);
    root.addView(reset, resetParams);

    TextView fixed = text(getString(R.string.bindings_dpad_fixed), 11f, COLOR_MUTED, false);
    LinearLayout.LayoutParams fixedParams = matchWidth();
    fixedParams.topMargin = dp(8);
    root.addView(fixed, fixedParams);
  }

  private void addBindingPair(LinearLayout root, BindableAction first, BindableAction second) {
    LinearLayout row = horizontalRow();
    addWeighted(row, bindingCard(first), true);
    addWeighted(row, bindingCard(second), false);

    LinearLayout.LayoutParams rowParams = matchWidth();
    rowParams.bottomMargin = dp(8);
    root.addView(row, rowParams);
  }

  private void addBindingSingle(LinearLayout root, BindableAction action) {
    LinearLayout card = bindingCard(action);
    LinearLayout.LayoutParams params = matchWidth();
    params.bottomMargin = dp(8);
    root.addView(card, params);
  }

  private LinearLayout bindingCard(BindableAction action) {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    card.setPadding(dp(13), dp(11), dp(11), dp(11));
    card.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 1, 10f));
    card.setClickable(true);
    card.setFocusable(true);
    card.setOnClickListener(
        v -> {
          pendingBinding = action;
          refreshBindingControls();
        });

    TextView label = text(getString(bindingTitle(action)), 13f, COLOR_TEXT, true);
    card.addView(
        label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

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
      case LANGUAGE_NEXT:
        return R.string.binding_language_next;
      case ACCEPT_SUGGESTION:
        return R.string.binding_accept_suggestion;
      case MINIMIZE:
        return R.string.binding_minimize;
      default:
        throw new IllegalArgumentException("Unknown binding action: " + action);
    }
  }

  private void refreshBindingControls() {
    if (preferences == null) {
      return;
    }

    ControllerBindings bindings = preferences.getControllerBindings();
    for (Map.Entry<BindableAction, TextView> entry : bindingChips.entrySet()) {
      BindableAction action = entry.getKey();
      TextView chip = entry.getValue();
      boolean waiting = action == pendingBinding;

      chip.setText(
          waiting
              ? getString(R.string.binding_press)
              : ControllerKeyNames.nameFor(bindings.getKeyCode(action)));
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
            getString(R.string.bindings_waiting, getString(bindingTitle(pendingBinding))));
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
    testField.setBackground(rounded(COLOR_SURFACE, COLOR_BORDER, 1, 12f));

    LinearLayout.LayoutParams params = matchWidth();
    params.bottomMargin = dp(18);
    root.addView(testField, params);
  }

  private void addSupportLink(LinearLayout root) {
    TextView support = text(getString(R.string.more_apps_support), 12f, COLOR_ACCENT, true);
    support.setGravity(Gravity.CENTER);
    support.setPadding(dp(12), dp(11), dp(12), dp(11));
    support.setClickable(true);
    support.setFocusable(true);
    support.setBackground(rounded(COLOR_SURFACE, 0xFF49386A, 1, 10f));
    support.setOnClickListener(
        v ->
            startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse("https://joelmomo.github.io/#support"))));
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
      KeyboardTheme theme =
          RuneThemes.ID_CUSTOM.equals(entry.getKey())
              ? RuneThemes.customTheme(preferences.getCustomThemeConfig())
              : RuneThemes.byId(entry.getKey());
      boolean selected = entry.getKey().equals(selectedTheme);
      entry
          .getValue()
          .setBackground(
              rounded(
                  selected ? 0xFF201A2D : COLOR_SURFACE,
                  selected ? theme.accent : COLOR_BORDER,
                  selected ? 2 : 1,
                  12f));

      View swatch = themeSwatches.get(entry.getKey());
      if (swatch != null) {
        GradientDrawable preview =
            new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {theme.backgroundTop, theme.backgroundBottom, theme.selectedFill});
        preview.setCornerRadius(dp(8));
        swatch.setBackground(preview);
      }
    }

    if (customThemePanel != null) {
      customThemePanel.setVisibility(
          RuneThemes.ID_CUSTOM.equals(selectedTheme) ? View.VISIBLE : View.GONE);
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

    refreshTypographyControls();
    refreshCustomThemeControls();
    refreshAppearanceSummary();
  }

  private LinearLayout horizontalRow() {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    return row;
  }

  private void addWeighted(LinearLayout row, View view, boolean first) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    if (!first) {
      params.setMarginStart(dp(9));
    }
    row.addView(view, params);
  }

  private TextView text(String value, float sizeSp, int color, boolean bold) {
    TextView view = new TextView(this);
    view.setText(value);
    view.setTextSize(sizeSp);
    view.setTextColor(color);
    if (bold) {
      view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    }
    return view;
  }

  private GradientDrawable rounded(int fill, int stroke, int strokeDp, float radiusDp) {
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
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
  }

  private LinearLayout.LayoutParams wrap() {
    return new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
  }

  private int dp(float value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }
}
