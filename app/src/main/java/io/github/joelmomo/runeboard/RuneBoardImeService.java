package io.github.joelmomo.runeboard;

import android.content.pm.ApplicationInfo;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import io.github.joelmomo.runeboard.controller.ControllerMapper;
import io.github.joelmomo.runeboard.editor.EditorActionResolver;
import io.github.joelmomo.runeboard.editor.EditorActionSpec;
import io.github.joelmomo.runeboard.editor.EditorInputPolicy;
import io.github.joelmomo.runeboard.keyboard.EditorCommand;
import io.github.joelmomo.runeboard.keyboard.SelectionController;
import io.github.joelmomo.runeboard.keyboard.WordNavigator;
import io.github.joelmomo.runeboard.language.KeyboardProfile;
import io.github.joelmomo.runeboard.settings.RunePreferences;
import io.github.joelmomo.runeboard.suggestion.AndroidSpellSuggestionSource;
import io.github.joelmomo.runeboard.suggestion.SuggestionPolicy;
import io.github.joelmomo.runeboard.suggestion.SuggestionResult;
import io.github.joelmomo.runeboard.suggestion.SuggestionSource;
import io.github.joelmomo.runeboard.suggestion.SuggestionText;
import io.github.joelmomo.runeboard.suggestion.WordContext;
import io.github.joelmomo.runeboard.theme.KeyboardFonts;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;
import java.util.ArrayList;
import java.util.List;

public final class RuneBoardImeService extends InputMethodService
    implements RuneKeyboardView.Listener {

  private static final String TAG = "RuneBoard";
  private static final int WORD_LOOKBACK = 96;

  private static volatile RuneBoardImeService activeInstance;

  private final SelectionController selectionController = new SelectionController();

  private RuneKeyboardView keyboardView;
  private RunePreferences preferences;
  private SuggestionSource suggestionSource;
  private SuggestionResult suggestionResult = SuggestionResult.empty("");
  private String suggestionProfileId;
  private int suggestionGeneration;
  private boolean debugLogging;

  @Override
  public void onCreate() {
    super.onCreate();
    preferences = new RunePreferences(this);
    debugLogging = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    activeInstance = this;
  }

  @Override
  public void onDestroy() {
    closeSuggestionSource();
    if (activeInstance == this) {
      activeInstance = null;
    }
    super.onDestroy();
  }

  public static RuneBoardImeService getActiveInstance() {
    return activeInstance;
  }

  public static void requestAppearanceRefresh() {
    RuneBoardImeService instance = activeInstance;
    if (instance != null) {
      instance.getMainExecutor().execute(instance::refreshAppearance);
    }
  }

  public boolean isControllerCaptureAvailable() {
    return keyboardView != null && isInputViewShown();
  }

  public boolean shouldCaptureControllerKey(int keyCode) {
    return keyboardView != null && keyboardView.shouldCaptureKeyCode(keyCode);
  }

  public boolean handleControllerKey(int keyCode) {
    return keyboardView != null && keyboardView.handleKeyCode(keyCode);
  }

  public boolean handleControllerKeyDown(int keyCode, int repeatCount) {
    return keyboardView != null && keyboardView.handleControllerKeyDown(keyCode, repeatCount);
  }

  public boolean handleControllerKeyUp(int keyCode) {
    return keyboardView != null && keyboardView.handleControllerKeyUp(keyCode);
  }

  public boolean isRepeatableControllerKey(int keyCode) {
    return keyboardView != null && keyboardView.isRepeatableKeyCode(keyCode);
  }

  @Override
  public View onCreateInputView() {
    setExtractViewShown(false);
    keyboardView = createKeyboardView();
    return keyboardView;
  }

  private RuneKeyboardView createKeyboardView() {
    KeyboardProfile profile = preferences.getKeyboardProfile();
    ensureSuggestionSource(profile);

    KeyboardTheme theme = preferences.getTheme();
    int initialOpacity = preferences.getBackgroundOpacity(theme);

    RuneKeyboardView view =
        new RuneKeyboardView(
            this,
            profile,
            theme,
            initialOpacity,
            new ControllerMapper(preferences.getControllerBindings()),
            KeyboardFonts.resolve(this, preferences.getKeyboardFontId()),
            preferences.getKeyTextColor(theme),
            preferences.isHapticFeedbackEnabled(),
            preferences.isSoundFeedbackEnabled());
    view.setListener(this);
    view.setEditorAction(resolveEditorAction(getCurrentInputEditorInfo()));
    view.setFocusable(true);
    view.setFocusableInTouchMode(true);
    view.requestFocus();
    return view;
  }

  private void refreshAppearance() {
    clearSuggestions();
    RuneKeyboardView refreshed = createKeyboardView();
    keyboardView = refreshed;
    setInputView(refreshed);
    if (isInputViewShown()) {
      refreshed.requestFocus();
    }
    scheduleContextRefresh();
  }

  @Override
  public boolean onEvaluateFullscreenMode() {
    return false;
  }

  @Override
  public boolean onEvaluateInputViewShown() {
    super.onEvaluateInputViewShown();
    // RuneBoard is intentionally a software IME even though the Thor
    // exposes physical controller/keyboard capabilities.
    return true;
  }

  @Override
  public void onStartInput(EditorInfo info, boolean restarting) {
    super.onStartInput(info, restarting);
    setExtractViewShown(false);

    if (info == null || info.inputType == InputType.TYPE_NULL) {
      return;
    }

    if (debugLogging) {
      Log.d(TAG, "autoShow inputType=" + info.inputType + " restarting=" + restarting);
    }

    // First ask Android to show the IME normally. The Thor can leave
    // a secondary-display IME stuck in READY_TO_SHOW while its display
    // token/insets animation is still being assigned, so perform one
    // delayed showWindow pass after that binding has settled.
    requestShowSelf(0);
    new Handler(Looper.getMainLooper())
        .postDelayed(
            () -> {
              if (getCurrentInputConnection() == null) {
                return;
              }
              showWindow(true);
              setExtractViewShown(false);
              if (keyboardView != null) {
                keyboardView.requestLayout();
                keyboardView.invalidate();
              }
            },
            140L);
  }

  @Override
  public void onWindowShown() {
    super.onWindowShown();
    setExtractViewShown(false);
  }

  @Override
  public void onStartInputView(EditorInfo info, boolean restarting) {
    super.onStartInputView(info, restarting);
    setExtractViewShown(false);
    if (keyboardView != null) {
      if (!restarting) {
        keyboardView.resetShiftMode();
      }
      keyboardView.setEditorAction(resolveEditorAction(info));
    }
    selectionController.reset();
    ensureSuggestionSource(preferences.getKeyboardProfile());
    scheduleContextRefresh();
  }

  @Override
  public void onFinishInputView(boolean finishingInput) {
    selectionController.reset();
    clearSuggestions();
    if (keyboardView != null) {
      keyboardView.resetShiftMode();
    }
    super.onFinishInputView(finishingInput);
  }

  @Override
  public void onUpdateSelection(
      int oldSelStart,
      int oldSelEnd,
      int newSelStart,
      int newSelEnd,
      int candidatesStart,
      int candidatesEnd) {
    super.onUpdateSelection(
        oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
    scheduleContextRefresh();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyboardView != null && keyboardView.shouldSuppressSyntheticDpad(event)) {
      if (debugLogging) {
        Log.d(TAG, "suppressedSyntheticDpad keyCode=" + keyCode);
      }
      return true;
    }

    if (keyboardView != null && keyboardView.shouldCaptureKeyCode(keyCode)) {
      return keyboardView.handleControllerKeyDown(keyCode, event.getRepeatCount());
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyboardView != null && keyboardView.shouldCaptureKeyCode(keyCode)) {
      return keyboardView.handleControllerKeyUp(keyCode);
    }
    return super.onKeyUp(keyCode, event);
  }

  @Override
  public boolean onGenericMotionEvent(MotionEvent event) {
    if (keyboardView != null && keyboardView.handleMotionEvent(event)) {
      return true;
    }
    return super.onGenericMotionEvent(event);
  }

  @Override
  public void onText(String text) {
    selectionController.reset();
    InputConnection connection = getCurrentInputConnection();
    if (connection != null) {
      connection.commitText(text, 1);
      scheduleContextRefresh();
    }
  }

  @Override
  public void onBackspace() {
    selectionController.reset();
    InputConnection connection = getCurrentInputConnection();
    if (connection != null) {
      connection.deleteSurroundingText(1, 0);
      scheduleContextRefresh();
    }
  }

  @Override
  public void onSpace() {
    selectionController.reset();
    InputConnection connection = getCurrentInputConnection();
    if (connection == null) {
      return;
    }

    WordContext context = currentWordContext(connection);
    if (preferences.isAutocorrectEnabled()
        && hasCollapsedSelection(connection)
        && SuggestionText.shouldAutoCorrect(context.word, suggestionResult)) {
      replaceCurrentWord(connection, context, suggestionResult.primary(), true);
    } else {
      connection.commitText(" ", 1);
    }

    clearSuggestions();
    scheduleContextRefresh();
  }

  @Override
  public void onEnter() {
    selectionController.reset();
    clearSuggestions();

    InputConnection connection = getCurrentInputConnection();
    if (connection == null) {
      return;
    }

    EditorActionSpec action = resolveEditorAction(getCurrentInputEditorInfo());
    if (action.performsEditorAction()) {
      boolean handled = connection.performEditorAction(action.actionId());
      if (debugLogging) {
        Log.d(
            TAG,
            "performEditorAction kind="
                + action.kind()
                + " actionId="
                + action.actionId()
                + " handled="
                + handled);
      }
      if (handled) {
        return;
      }
    }

    connection.commitText("\n", 1);
    scheduleContextRefresh();
  }

  @Override
  public void onMoveCursor(int direction) {
    selectionController.reset();
    InputConnection connection = getCurrentInputConnection();
    if (connection == null) {
      return;
    }

    ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
    if (extracted != null && extracted.text != null && extracted.selectionStart >= 0) {
      int next =
          Math.max(
              0,
              Math.min(
                  extracted.text.length(), extracted.selectionStart + (direction < 0 ? -1 : 1)));
      connection.setSelection(next, next);
      scheduleCapitalizationRefresh();
      return;
    }

    int keyCode = direction < 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
    long now = SystemClock.uptimeMillis();
    connection.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0));
    connection.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0));
    scheduleCapitalizationRefresh();
  }

  @Override
  public void onMoveWord(int direction) {
    selectionController.reset();
    InputConnection connection = getCurrentInputConnection();
    if (connection == null) {
      return;
    }

    ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
    if (extracted == null || extracted.text == null || extracted.selectionStart < 0) {
      return;
    }

    String text = extracted.text.toString();
    int localSelection = extracted.selectionStart - extracted.startOffset;
    if (localSelection < 0 || localSelection > text.length()) {
      return;
    }

    int localNext = WordNavigator.move(text, localSelection, direction);
    int next = extracted.startOffset + localNext;
    connection.setSelection(next, next);
    scheduleCapitalizationRefresh();
  }

  @Override
  public void onNextLanguage() {
    selectionController.reset();
    preferences.cycleKeyboardProfile();
    getMainExecutor().execute(this::refreshAppearance);
  }

  @Override
  public void onAcceptSuggestion() {
    selectionController.reset();
    String primary = suggestionResult.primary();
    if (primary != null) {
      applySuggestion(primary);
    }
  }

  @Override
  public void onEditorCommand(EditorCommand command) {
    InputConnection connection = getCurrentInputConnection();
    if (connection == null || command == null) {
      return;
    }

    clearSuggestions();
    boolean textChanged = false;
    switch (command) {
      case SELECT_ALL:
        selectionController.reset();
        connection.performContextMenuAction(android.R.id.selectAll);
        break;
      case CUT:
        selectionController.reset();
        textChanged = connection.performContextMenuAction(android.R.id.cut);
        break;
      case COPY:
        connection.performContextMenuAction(android.R.id.copy);
        break;
      case PASTE:
        selectionController.reset();
        textChanged = connection.performContextMenuAction(android.R.id.paste);
        break;
      case UNDO:
        selectionController.reset();
        textChanged = connection.performContextMenuAction(android.R.id.undo);
        break;
      case REDO:
        selectionController.reset();
        textChanged = connection.performContextMenuAction(android.R.id.redo);
        break;
      case HOME:
        selectionController.reset();
        sendEditorKey(connection, KeyEvent.KEYCODE_MOVE_HOME);
        break;
      case END:
        selectionController.reset();
        sendEditorKey(connection, KeyEvent.KEYCODE_MOVE_END);
        break;
      case CURSOR_LEFT:
        onMoveCursor(-1);
        break;
      case CURSOR_RIGHT:
        onMoveCursor(1);
        break;
      case WORD_LEFT:
        onMoveWord(-1);
        break;
      case WORD_RIGHT:
        onMoveWord(1);
        break;
      case SELECT_LEFT:
        extendSelection(connection, -1, false);
        break;
      case SELECT_RIGHT:
        extendSelection(connection, 1, false);
        break;
      case SELECT_WORD_LEFT:
        extendSelection(connection, -1, true);
        break;
      case SELECT_WORD_RIGHT:
        extendSelection(connection, 1, true);
        break;
      case DELETE_FORWARD:
        selectionController.reset();
        connection.deleteSurroundingText(0, 1);
        textChanged = true;
        break;
      default:
        break;
    }

    if (textChanged) {
      scheduleContextRefresh();
    } else {
      scheduleCapitalizationRefresh();
    }
  }

  @Override
  public void onSuggestionSelected(String suggestion) {
    selectionController.reset();
    applySuggestion(suggestion);
  }

  @Override
  public void onMinimizedChanged(boolean minimized) {
    // AYN Thor firmware .377 honors the requested input-view resize.
  }

  @Override
  public void onBackgroundOpacityChanged(int opacity) {
    preferences.setBackgroundOpacity(opacity);
  }

  private void ensureSuggestionSource(KeyboardProfile profile) {
    if (!preferences.areSuggestionsEnabled()) {
      closeSuggestionSource();
      return;
    }

    if (suggestionSource != null
        && profile.id.equals(suggestionProfileId)
        && suggestionSource.isAvailable()) {
      return;
    }

    closeSuggestionSource();
    suggestionProfileId = profile.id;
    suggestionGeneration++;
    suggestionSource = new AndroidSpellSuggestionSource(this, profile.locale);
  }

  private void closeSuggestionSource() {
    suggestionGeneration++;
    if (suggestionSource != null) {
      suggestionSource.close();
      suggestionSource = null;
    }
    suggestionProfileId = null;
    suggestionResult = SuggestionResult.empty("");
  }

  private void requestSuggestions() {
    if (keyboardView == null
        || !isInputViewShown()
        || !preferences.areSuggestionsEnabled()
        || !supportsSuggestions(getCurrentInputEditorInfo())) {
      clearSuggestions();
      return;
    }

    SuggestionSource source = suggestionSource;
    if (source == null || !source.isAvailable()) {
      ensureSuggestionSource(preferences.getKeyboardProfile());
      source = suggestionSource;
    }

    if (source == null || !source.isAvailable()) {
      clearSuggestions();
      return;
    }

    InputConnection connection = getCurrentInputConnection();
    if (connection == null || !hasCollapsedSelection(connection)) {
      clearSuggestions();
      return;
    }

    WordContext context = currentWordContext(connection);
    if (context.length < 2) {
      clearSuggestions();
      return;
    }

    String requestedWord = context.word;
    int generation = suggestionGeneration;
    source.request(
        requestedWord,
        result -> {
          if (generation != suggestionGeneration || keyboardView == null) {
            return;
          }

          InputConnection currentConnection = getCurrentInputConnection();
          if (currentConnection == null) {
            return;
          }

          WordContext current = currentWordContext(currentConnection);
          if (!current.word.equalsIgnoreCase(result.word)) {
            return;
          }

          suggestionResult = result;
          KeyboardProfile profile = preferences.getKeyboardProfile();
          List<String> display = new ArrayList<>();
          for (String candidate : result.candidates) {
            display.add(SuggestionText.adaptCase(candidate, current.word, profile.locale));
          }

          if (debugLogging) {
            Log.d(
                TAG,
                "suggestions word="
                    + result.word
                    + " candidates="
                    + display
                    + " typo="
                    + result.looksLikeTypo
                    + " recommended="
                    + result.recommended);
          }

          keyboardView.setSuggestions(display, result.recommended && result.looksLikeTypo);
        });
  }

  private void clearSuggestions() {
    suggestionResult = SuggestionResult.empty("");
    if (keyboardView != null) {
      keyboardView.clearSuggestions();
    }
  }

  private void applySuggestion(String suggestion) {
    InputConnection connection = getCurrentInputConnection();
    if (connection == null || suggestion == null) {
      return;
    }

    if (!hasCollapsedSelection(connection)) {
      clearSuggestions();
      return;
    }

    WordContext context = currentWordContext(connection);
    if (context.length == 0
        || suggestionResult.word.isEmpty()
        || !context.word.equalsIgnoreCase(suggestionResult.word)) {
      clearSuggestions();
      return;
    }

    replaceCurrentWord(connection, context, suggestion, false);
    clearSuggestions();
    scheduleContextRefresh();
  }

  private void replaceCurrentWord(
      InputConnection connection, WordContext context, String suggestion, boolean appendSpace) {
    KeyboardProfile profile = preferences.getKeyboardProfile();
    String replacement = SuggestionText.adaptCase(suggestion, context.word, profile.locale);

    connection.beginBatchEdit();
    try {
      connection.deleteSurroundingText(context.length, 0);
      connection.commitText(replacement + (appendSpace ? " " : ""), 1);
    } finally {
      connection.endBatchEdit();
    }
  }

  private void extendSelection(InputConnection connection, int direction, boolean byWord) {
    ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
    if (extracted == null
        || extracted.text == null
        || extracted.selectionStart < 0
        || extracted.selectionEnd < 0) {
      selectionController.reset();
      return;
    }

    String text = extracted.text.toString();
    int localStart = extracted.selectionStart - extracted.startOffset;
    int localEnd = extracted.selectionEnd - extracted.startOffset;
    if (localStart < 0 || localStart > text.length() || localEnd < 0 || localEnd > text.length()) {
      selectionController.reset();
      return;
    }

    SelectionController.Range range =
        selectionController.extend(text, localStart, localEnd, direction, byWord);
    connection.setSelection(
        extracted.startOffset + range.anchor, extracted.startOffset + range.caret);
  }

  private void sendEditorKey(InputConnection connection, int keyCode) {
    long now = SystemClock.uptimeMillis();
    connection.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0));
    connection.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0));
  }

  private WordContext currentWordContext(InputConnection connection) {
    CharSequence before = connection.getTextBeforeCursor(WORD_LOOKBACK, 0);
    return WordContext.trailing(before);
  }

  private boolean hasCollapsedSelection(InputConnection connection) {
    ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
    return extracted != null
        && extracted.selectionStart >= 0
        && extracted.selectionEnd >= 0
        && extracted.selectionStart == extracted.selectionEnd;
  }

  private void scheduleContextRefresh() {
    getMainExecutor()
        .execute(
            () -> {
              refreshAutoCapitalization();
              requestSuggestions();
            });
  }

  private void scheduleCapitalizationRefresh() {
    getMainExecutor().execute(this::refreshAutoCapitalization);
  }

  private void refreshAutoCapitalization() {
    RuneKeyboardView view = keyboardView;
    if (view == null) {
      return;
    }

    EditorInfo info = getCurrentInputEditorInfo();
    int requestedModes = info == null ? 0 : EditorInputPolicy.capitalizationModes(info.inputType);
    if (requestedModes == 0) {
      view.setAutoCapitalization(false);
      return;
    }

    InputConnection connection = getCurrentInputConnection();
    boolean enabled =
        connection != null && (connection.getCursorCapsMode(requestedModes) & requestedModes) != 0;
    view.setAutoCapitalization(enabled);
  }

  private EditorActionSpec resolveEditorAction(EditorInfo info) {
    if (info == null) {
      return EditorActionSpec.enter();
    }
    return EditorActionResolver.resolve(info.imeOptions, info.actionId, info.actionLabel);
  }

  private boolean supportsSuggestions(EditorInfo info) {
    return info != null && SuggestionPolicy.supportsInputType(info.inputType);
  }
}
