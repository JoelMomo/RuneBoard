package io.github.joelmomo.runeboard;

import android.content.pm.ApplicationInfo;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import io.github.joelmomo.runeboard.controller.ControllerMapper;
import io.github.joelmomo.runeboard.keyboard.WordNavigator;
import io.github.joelmomo.runeboard.language.KeyboardProfile;
import io.github.joelmomo.runeboard.settings.RunePreferences;
import io.github.joelmomo.runeboard.suggestion.AndroidSpellSuggestionSource;
import io.github.joelmomo.runeboard.suggestion.SuggestionResult;
import io.github.joelmomo.runeboard.suggestion.SuggestionPolicy;
import io.github.joelmomo.runeboard.suggestion.SuggestionSource;
import io.github.joelmomo.runeboard.suggestion.SuggestionText;
import io.github.joelmomo.runeboard.suggestion.WordContext;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;

import java.util.ArrayList;
import java.util.List;

public final class RuneBoardImeService extends InputMethodService
        implements RuneKeyboardView.Listener {

    private static final String TAG = "RuneBoard";
    private static final int WORD_LOOKBACK = 96;

    private static volatile RuneBoardImeService activeInstance;

    private RuneKeyboardView keyboardView;
    private RunePreferences preferences;
    private SuggestionSource suggestionSource;
    private SuggestionResult suggestionResult =
            SuggestionResult.empty("");
    private String suggestionProfileId;
    private int suggestionGeneration;
    private boolean debugLogging;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new RunePreferences(this);
        debugLogging = (getApplicationInfo().flags
                & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
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
        return keyboardView != null
                && keyboardView.shouldCaptureKeyCode(keyCode);
    }

    public boolean handleControllerKey(int keyCode) {
        return keyboardView != null
                && keyboardView.handleKeyCode(keyCode);
    }

    public boolean isRepeatableControllerKey(int keyCode) {
        return keyboardView != null
                && keyboardView.isRepeatableKeyCode(keyCode);
    }

    @Override
    public View onCreateInputView() {
        keyboardView = createKeyboardView();
        return keyboardView;
    }

    private RuneKeyboardView createKeyboardView() {
        KeyboardProfile profile = preferences.getKeyboardProfile();
        ensureSuggestionSource(profile);

        KeyboardTheme theme = preferences.getTheme();
        int initialOpacity = preferences.getBackgroundOpacity(theme);

        RuneKeyboardView view = new RuneKeyboardView(
                this,
                profile,
                theme,
                initialOpacity,
                new ControllerMapper(preferences.getControllerBindings()));
        view.setListener(this);
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
        getMainExecutor().execute(this::requestSuggestions);
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;
    }

    @Override
    public void onStartInputView(
            EditorInfo info,
            boolean restarting) {
        super.onStartInputView(info, restarting);
        ensureSuggestionSource(preferences.getKeyboardProfile());
        getMainExecutor().execute(this::requestSuggestions);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        clearSuggestions();
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
                oldSelStart,
                oldSelEnd,
                newSelStart,
                newSelEnd,
                candidatesStart,
                candidatesEnd);
        getMainExecutor().execute(this::requestSuggestions);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyboardView != null
                && event.getRepeatCount() == 0
                && keyboardView.handleKeyCode(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (keyboardView != null
                && keyboardView.handleMotionEvent(event)) {
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onText(String text) {
        InputConnection connection = getCurrentInputConnection();
        if (connection != null) {
            connection.commitText(text, 1);
            getMainExecutor().execute(this::requestSuggestions);
        }
    }

    @Override
    public void onBackspace() {
        InputConnection connection = getCurrentInputConnection();
        if (connection != null) {
            connection.deleteSurroundingText(1, 0);
            getMainExecutor().execute(this::requestSuggestions);
        }
    }

    @Override
    public void onSpace() {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }

        WordContext context = currentWordContext(connection);
        if (preferences.isAutocorrectEnabled()
                && hasCollapsedSelection(connection)
                && SuggestionText.shouldAutoCorrect(
                        context.word,
                        suggestionResult)) {
            replaceCurrentWord(
                    connection,
                    context,
                    suggestionResult.primary(),
                    true);
        } else {
            connection.commitText(" ", 1);
        }

        clearSuggestions();
    }

    @Override
    public void onEnter() {
        clearSuggestions();

        EditorInfo info = getCurrentInputEditorInfo();
        int action = info == null
                ? EditorInfo.IME_ACTION_NONE
                : info.imeOptions & EditorInfo.IME_MASK_ACTION;

        if (action != EditorInfo.IME_ACTION_NONE
                && action != EditorInfo.IME_ACTION_UNSPECIFIED
                && sendDefaultEditorAction(false)) {
            return;
        }

        InputConnection connection = getCurrentInputConnection();
        if (connection != null) {
            connection.commitText("\n", 1);
        }
    }

    @Override
    public void onMoveCursor(int direction) {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }

        ExtractedText extracted =
                connection.getExtractedText(new ExtractedTextRequest(), 0);
        if (extracted != null
                && extracted.text != null
                && extracted.selectionStart >= 0) {
            int next = Math.max(
                    0,
                    Math.min(
                            extracted.text.length(),
                            extracted.selectionStart
                                    + (direction < 0 ? -1 : 1)));
            connection.setSelection(next, next);
            return;
        }

        int keyCode = direction < 0
                ? KeyEvent.KEYCODE_DPAD_LEFT
                : KeyEvent.KEYCODE_DPAD_RIGHT;
        long now = SystemClock.uptimeMillis();
        connection.sendKeyEvent(new KeyEvent(
                now,
                now,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0));
        connection.sendKeyEvent(new KeyEvent(
                now,
                now,
                KeyEvent.ACTION_UP,
                keyCode,
                0));
    }

    @Override
    public void onMoveWord(int direction) {
        InputConnection connection = getCurrentInputConnection();
        if (connection == null) {
            return;
        }

        ExtractedText extracted =
                connection.getExtractedText(new ExtractedTextRequest(), 0);
        if (extracted == null
                || extracted.text == null
                || extracted.selectionStart < 0) {
            return;
        }

        String text = extracted.text.toString();
        int localSelection = extracted.selectionStart - extracted.startOffset;
        if (localSelection < 0 || localSelection > text.length()) {
            return;
        }

        int localNext = WordNavigator.move(
                text,
                localSelection,
                direction);
        int next = extracted.startOffset + localNext;
        connection.setSelection(next, next);
    }

    @Override
    public void onNextLanguage() {
        preferences.cycleKeyboardProfile();
        getMainExecutor().execute(this::refreshAppearance);
    }

    @Override
    public void onAcceptSuggestion() {
        String primary = suggestionResult.primary();
        if (primary != null) {
            applySuggestion(primary);
        }
    }

    @Override
    public void onSuggestionSelected(String suggestion) {
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
        suggestionSource = new AndroidSpellSuggestionSource(
                this,
                profile.locale);
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
        source.request(requestedWord, result -> {
            if (generation != suggestionGeneration
                    || keyboardView == null) {
                return;
            }

            InputConnection currentConnection =
                    getCurrentInputConnection();
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
                display.add(SuggestionText.adaptCase(
                        candidate,
                        current.word,
                        profile.locale));
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

            keyboardView.setSuggestions(
                    display,
                    result.recommended && result.looksLikeTypo);
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
        getMainExecutor().execute(this::requestSuggestions);
    }

    private void replaceCurrentWord(
            InputConnection connection,
            WordContext context,
            String suggestion,
            boolean appendSpace) {
        KeyboardProfile profile = preferences.getKeyboardProfile();
        String replacement = SuggestionText.adaptCase(
                suggestion,
                context.word,
                profile.locale);

        connection.beginBatchEdit();
        try {
            connection.deleteSurroundingText(context.length, 0);
            connection.commitText(
                    replacement + (appendSpace ? " " : ""),
                    1);
        } finally {
            connection.endBatchEdit();
        }
    }

    private WordContext currentWordContext(InputConnection connection) {
        CharSequence before = connection.getTextBeforeCursor(
                WORD_LOOKBACK,
                0);
        return WordContext.trailing(before);
    }

    private boolean hasCollapsedSelection(InputConnection connection) {
        ExtractedText extracted =
                connection.getExtractedText(new ExtractedTextRequest(), 0);
        return extracted != null
                && extracted.selectionStart >= 0
                && extracted.selectionEnd >= 0
                && extracted.selectionStart == extracted.selectionEnd;
    }

    private boolean supportsSuggestions(EditorInfo info) {
        return info != null
                && SuggestionPolicy.supportsInputType(info.inputType);
    }
}
