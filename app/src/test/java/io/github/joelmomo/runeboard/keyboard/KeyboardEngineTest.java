package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.github.joelmomo.runeboard.controller.ControllerAction;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public final class KeyboardEngineTest {

    @Test
    public void shiftIsOneShotForText() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(KeyboardLayouts.qwerty(), output);

        engine.handle(ControllerAction.SHIFT);
        engine.handle(ControllerAction.PRESS_SELECTED);
        engine.handle(ControllerAction.PRESS_SELECTED);

        assertEquals(List.of("Q", "q"), output.text);
        assertFalse(engine.getState().isShifted());
    }

    @Test
    public void capsLockPersistsUntilShiftCyclesOff() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(KeyboardLayouts.qwerty(), output);

        engine.handle(ControllerAction.SHIFT);
        engine.handle(ControllerAction.SHIFT);
        engine.handle(ControllerAction.PRESS_SELECTED);
        engine.handle(ControllerAction.PRESS_SELECTED);

        assertEquals(List.of("Q", "Q"), output.text);
        assertTrue(engine.getState().isCapsLocked());

        engine.handle(ControllerAction.SHIFT);
        engine.handle(ControllerAction.PRESS_SELECTED);

        assertEquals(List.of("Q", "Q", "q"), output.text);
        assertFalse(engine.getState().isShifted());
    }

    @Test
    public void directEditingActionsReachOutput() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(KeyboardLayouts.qwerty(), output);

        engine.handle(ControllerAction.SPACE);
        engine.handle(ControllerAction.BACKSPACE);
        engine.handle(ControllerAction.ENTER);
        engine.handle(ControllerAction.CURSOR_LEFT);
        engine.handle(ControllerAction.CURSOR_RIGHT);
        engine.handle(ControllerAction.WORD_LEFT);
        engine.handle(ControllerAction.WORD_RIGHT);

        assertEquals(1, output.spaces);
        assertEquals(1, output.backspaces);
        assertEquals(1, output.enters);
        assertEquals(List.of(-1, 1), output.cursorMoves);
        assertEquals(List.of(-1, 1), output.wordMoves);
    }

    @Test
    public void minimizedKeyboardOnlyCapturesRestoreActions() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(KeyboardLayouts.qwerty(), output);

        assertEquals(
                KeyboardEngine.Update.LAYOUT,
                engine.handle(ControllerAction.TOGGLE_MINIMIZE));
        assertTrue(engine.getState().isMinimized());

        assertFalse(engine.shouldCapture(ControllerAction.MOVE_LEFT));
        assertFalse(engine.shouldCapture(ControllerAction.MOVE_RIGHT));
        assertFalse(engine.shouldCapture(ControllerAction.BACKSPACE));
        assertFalse(engine.shouldCapture(ControllerAction.SPACE));
        assertFalse(engine.shouldCapture(ControllerAction.SHIFT));
        assertFalse(engine.shouldCapture(ControllerAction.CURSOR_LEFT));
        assertFalse(engine.shouldCapture(ControllerAction.CURSOR_RIGHT));
        assertFalse(engine.shouldCapture(ControllerAction.ACCEPT_SUGGESTION));

        assertTrue(engine.shouldCapture(ControllerAction.PRESS_SELECTED));
        assertFalse(engine.shouldCapture(ControllerAction.PRESS_CENTER));
        assertFalse(engine.shouldCapture(ControllerAction.ENTER));
        assertTrue(engine.shouldCapture(ControllerAction.TOGGLE_MINIMIZE));
    }

    @Test
    public void pressSelectedRestoresMinimizedKeyboard() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(KeyboardLayouts.qwerty(), output);

        engine.handle(ControllerAction.TOGGLE_MINIMIZE);
        KeyboardEngine.Update update =
                engine.handle(ControllerAction.PRESS_SELECTED);

        assertEquals(KeyboardEngine.Update.LAYOUT, update);
        assertFalse(engine.getState().isMinimized());
        assertEquals(List.of(true, false), output.minimizedStates);
    }

    @Test
    public void languageActionReachesOutput() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(KeyboardLayouts.qwerty(), output);

        engine.handle(ControllerAction.LANGUAGE_NEXT);

        assertEquals(1, output.languageChanges);
    }

    @Test
    public void acceptSuggestionActionReachesOutput() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(KeyboardLayouts.qwerty(), output);

        engine.handle(ControllerAction.ACCEPT_SUGGESTION);

        assertEquals(1, output.suggestionAccepts);
    }

    @Test
    public void opacityStartsFromConfiguredValueAndNotifiesOutput() {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine =
                new KeyboardEngine(
                        KeyboardLayouts.qwerty(),
                        output,
                        90);

        engine.getState().select(4, 4);
        assertEquals(90, engine.getState().getOpacity());

        assertEquals(
                KeyboardEngine.Update.VISUAL,
                engine.handle(ControllerAction.PRESS_SELECTED));

        assertEquals(0, engine.getState().getOpacity());
        assertEquals(List.of(0), output.backgroundOpacities);
    }

    private static final class RecordingOutput
            implements KeyboardEngine.Output {

        final List<String> text = new ArrayList<>();
        final List<Integer> cursorMoves = new ArrayList<>();
        final List<Integer> wordMoves = new ArrayList<>();
        final List<Boolean> minimizedStates = new ArrayList<>();
        final List<Integer> backgroundOpacities = new ArrayList<>();
        int languageChanges;
        int suggestionAccepts;

        int backspaces;
        int spaces;
        int enters;

        @Override
        public void onText(String value) {
            text.add(value);
        }

        @Override
        public void onBackspace() {
            backspaces++;
        }

        @Override
        public void onSpace() {
            spaces++;
        }

        @Override
        public void onEnter() {
            enters++;
        }

        @Override
        public void onMoveCursor(int direction) {
            cursorMoves.add(direction);
        }

        @Override
        public void onMoveWord(int direction) {
            wordMoves.add(direction);
        }

        @Override
        public void onNextLanguage() {
            languageChanges++;
        }
        @Override
        public void onAcceptSuggestion() {
            suggestionAccepts++;
        }

        @Override
        public void onMinimizedChanged(boolean minimized) {
            minimizedStates.add(minimized);
        }

        @Override
        public void onBackgroundOpacityChanged(int opacity) {
            backgroundOpacities.add(opacity);
        }
    }
}
