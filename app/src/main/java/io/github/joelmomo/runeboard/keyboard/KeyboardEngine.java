package io.github.joelmomo.runeboard.keyboard;

import io.github.joelmomo.runeboard.controller.ControllerAction;
import io.github.joelmomo.runeboard.theme.BackgroundOpacity;

import java.util.Locale;

public final class KeyboardEngine {

    public interface Output {
        void onText(String text);
        void onBackspace();
        void onSpace();
        void onEnter();
        void onMoveCursor(int direction);
        void onMoveWord(int direction);
        void onNextLanguage();
        void onAcceptSuggestion();
        void onEditorCommand(EditorCommand command);
        void onMinimizedChanged(boolean minimized);
        void onBackgroundOpacityChanged(int opacity);
    }

    public enum Update {
        NONE,
        VISUAL,
        GEOMETRY,
        LAYOUT
    }

    private final KeyboardState state;
    private final Output output;
    private final Locale locale;

    public KeyboardEngine(KeyboardLayout layout, Output output) {
        this(
                layout,
                layout,
                KeyboardLayouts.editorLayout(),
                output,
                BackgroundOpacity.defaultValue(),
                Locale.ROOT);
    }

    public KeyboardEngine(
            KeyboardLayout layout,
            Output output,
            int initialOpacity) {
        this(
                layout,
                layout,
                KeyboardLayouts.editorLayout(),
                output,
                initialOpacity,
                Locale.ROOT);
    }

    public KeyboardEngine(
            KeyboardLayout layout,
            Output output,
            int initialOpacity,
            Locale locale) {
        this(
                layout,
                layout,
                KeyboardLayouts.editorLayout(),
                output,
                initialOpacity,
                locale);
    }

    public KeyboardEngine(
            KeyboardLayout alphabetLayout,
            KeyboardLayout symbolLayout,
            Output output,
            int initialOpacity,
            Locale locale) {
        this(
                alphabetLayout,
                symbolLayout,
                KeyboardLayouts.editorLayout(),
                output,
                initialOpacity,
                locale);
    }

    public KeyboardEngine(
            KeyboardLayout alphabetLayout,
            KeyboardLayout symbolLayout,
            KeyboardLayout editorLayout,
            Output output,
            int initialOpacity,
            Locale locale) {
        this.state = new KeyboardState(
                alphabetLayout,
                symbolLayout,
                editorLayout,
                initialOpacity);
        this.output = output;
        this.locale = locale == null ? Locale.ROOT : locale;
    }

    public KeyboardState getState() {
        return state;
    }

    public Update setAutoShifted(boolean shifted) {
        return state.setAutoShifted(shifted)
                ? Update.VISUAL
                : Update.NONE;
    }

    public Update resetShiftMode() {
        return state.resetShiftMode()
                ? Update.VISUAL
                : Update.NONE;
    }

    public Update select(int row, int col) {
        if (state.getSelectedRow() == row && state.getSelectedCol() == col) {
            return Update.NONE;
        }
        state.select(row, col);
        return Update.VISUAL;
    }

    public boolean shouldCapture(ControllerAction action) {
        if (action == ControllerAction.NONE) {
            return false;
        }
        if (!state.isMinimized()) {
            return true;
        }
        return action == ControllerAction.PRESS_SELECTED
                || action == ControllerAction.TOGGLE_MINIMIZE;
    }

    public Update handle(ControllerAction action) {
        if (!shouldCapture(action)) {
            return Update.NONE;
        }

        if (state.isMinimized()) {
            return setMinimized(false);
        }

        switch (action) {
            case MOVE_LEFT:
                return state.move(-1, 0) ? Update.VISUAL : Update.NONE;
            case MOVE_RIGHT:
                return state.move(1, 0) ? Update.VISUAL : Update.NONE;
            case MOVE_UP:
                return state.move(0, -1) ? Update.VISUAL : Update.NONE;
            case MOVE_DOWN:
                return state.move(0, 1) ? Update.VISUAL : Update.NONE;
            case PRESS_SELECTED:
            case PRESS_CENTER:
                return pressSelected();
            case BACKSPACE:
                output.onBackspace();
                return Update.NONE;
            case SPACE:
                output.onSpace();
                return Update.NONE;
            case SHIFT:
                state.advanceShiftMode();
                return Update.VISUAL;
            case CURSOR_LEFT:
                output.onMoveCursor(-1);
                return Update.NONE;
            case CURSOR_RIGHT:
                output.onMoveCursor(1);
                return Update.NONE;
            case WORD_LEFT:
                output.onMoveWord(-1);
                return Update.NONE;
            case WORD_RIGHT:
                output.onMoveWord(1);
                return Update.NONE;
            case ENTER:
                output.onEnter();
                return Update.NONE;
            case LANGUAGE_NEXT:
                output.onNextLanguage();
                return Update.NONE;
            case ACCEPT_SUGGESTION:
                output.onAcceptSuggestion();
                return Update.NONE;
            case TOGGLE_MINIMIZE:
                return setMinimized(true);
            case NONE:
            default:
                return Update.NONE;
        }
    }

    public Update commitTextVariant(String text) {
        if (text == null || text.isEmpty()) {
            return Update.NONE;
        }
        output.onText(text);
        return state.consumeOneShotShift()
                ? Update.VISUAL
                : Update.NONE;
    }

    public Update pressSelected() {
        if (state.isMinimized()) {
            return setMinimized(false);
        }

        KeyboardKey key = state.getSelectedKey();
        switch (key.getType()) {
            case TEXT:
                String text = key.getText();
                if (state.isShifted()
                        && !state.isSymbols()
                        && !state.isEditing()
                        && Character.isLetter(text.charAt(0))) {
                    text = text.toUpperCase(locale);
                }
                output.onText(text);
                if (state.consumeOneShotShift()) {
                    return Update.VISUAL;
                }
                return Update.NONE;
            case SHIFT:
                state.advanceShiftMode();
                return Update.VISUAL;
            case MODE:
                state.toggleSymbols();
                return Update.GEOMETRY;
            case EDIT:
                state.toggleEditing();
                return Update.GEOMETRY;
            case COMMAND:
                output.onEditorCommand(key.getCommand());
                return Update.NONE;
            case SPACE:
                output.onSpace();
                return Update.NONE;
            case BACKSPACE:
                output.onBackspace();
                return Update.NONE;
            case ENTER:
                output.onText("\n");
                return Update.NONE;
            case OPACITY:
                state.cycleOpacity();
                output.onBackgroundOpacityChanged(state.getOpacity());
                return Update.VISUAL;
            case MINIMIZE:
                return setMinimized(true);
            default:
                return Update.NONE;
        }
    }

    private Update setMinimized(boolean minimized) {
        if (!state.setMinimized(minimized)) {
            return Update.NONE;
        }
        output.onMinimizedChanged(minimized);
        return Update.LAYOUT;
    }
}
