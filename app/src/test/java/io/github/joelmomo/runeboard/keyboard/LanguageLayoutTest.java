package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;

import io.github.joelmomo.runeboard.controller.ControllerAction;
import io.github.joelmomo.runeboard.language.KeyboardProfile;
import io.github.joelmomo.runeboard.language.KeyboardProfiles;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public final class LanguageLayoutTest {

    @Test
    public void spanishLayoutContainsEnye() {
        KeyboardLayout layout = KeyboardLayouts.spanishQwerty();

        assertEquals(10, layout.getRow(3).size());
        assertEquals("\u00f1", layout.getKey(3, 9).getText());
    }

    @Test
    public void frenchLayoutIsAzerty() {
        KeyboardLayout layout = KeyboardLayouts.frenchAzerty();

        assertEquals("a", layout.getKey(2, 0).getText());
        assertEquals("z", layout.getKey(2, 1).getText());
        assertEquals("q", layout.getKey(3, 0).getText());
    }

    @Test
    public void russianLayoutUsesJcukenCharacters() {
        KeyboardLayout layout = KeyboardLayouts.russianJcuken();

        assertEquals(12, layout.getRow(2).size());
        assertEquals("\u0439", layout.getKey(2, 0).getText());
        assertEquals("\u044a", layout.getKey(2, 11).getText());
        assertEquals("\u044f", layout.getKey(4, 0).getText());
    }

    @Test
    public void alphabetLayoutsExposeCommaAndPeriod() {
        KeyboardLayout layout = KeyboardLayouts.englishQwerty();

        assertEquals(",", layout.getKey(5, 2).getText());
        assertEquals(".", layout.getKey(5, 4).getText());
    }

    @Test
    public void spanishSymbolsContainAccentsAndInvertedPunctuation() {
        KeyboardLayout layout = KeyboardLayouts.spanishSymbols();

        assertEquals("\u00e1", layout.getKey(3, 0).getText());
        assertEquals("\u00f1", layout.getKey(3, 6).getText());
        assertEquals("\u00bf", layout.getKey(3, 7).getText());
        assertEquals("\u00a1", layout.getKey(3, 8).getText());
    }

    @Test
    public void frenchSymbolsContainCommonDiacritics() {
        KeyboardLayout layout = KeyboardLayouts.frenchSymbols();

        assertEquals("\u00e9", layout.getKey(3, 0).getText());
        assertEquals("\u00e7", layout.getKey(3, 3).getText());
        assertEquals("\u00fb", layout.getKey(3, 9).getText());
    }

    @Test
    public void russianSymbolsExposeYoVariants() {
        KeyboardLayout layout = KeyboardLayouts.russianSymbols();

        assertEquals("\u0451", layout.getKey(3, 0).getText());
        assertEquals("\u0401", layout.getKey(3, 1).getText());
    }

    @Test
    public void localeAwareShiftUppercasesSpanishAndRussian() {
        assertEquals("\u00d1", shiftedText(
                KeyboardProfiles.byId(KeyboardProfiles.ID_ES_ES),
                3,
                9));
        assertEquals("\u0419", shiftedText(
                KeyboardProfiles.byId(KeyboardProfiles.ID_RU_RU),
                2,
                0));
    }

    private String shiftedText(
            KeyboardProfile profile,
            int row,
            int column) {
        RecordingOutput output = new RecordingOutput();
        KeyboardEngine engine = new KeyboardEngine(
                profile.layout,
                output,
                255,
                profile.locale);
        engine.getState().select(row, column);
        engine.handle(ControllerAction.SHIFT);
        engine.handle(ControllerAction.PRESS_SELECTED);
        return output.text.get(0);
    }

    private static final class RecordingOutput
            implements KeyboardEngine.Output {

        final List<String> text = new ArrayList<>();

        @Override
        public void onText(String value) {
            text.add(value);
        }

        @Override
        public void onBackspace() {
        }

        @Override
        public void onSpace() {
        }

        @Override
        public void onEnter() {
        }

        @Override
        public void onMoveCursor(int direction) {
        }

        @Override
        public void onMoveWord(int direction) {
        }

        @Override
        public void onNextLanguage() {
        }
        @Override
        public void onAcceptSuggestion() {
        }

        @Override
        public void onEditorCommand(EditorCommand command) {
        }

        @Override
        public void onMinimizedChanged(boolean minimized) {
        }

        @Override
        public void onBackgroundOpacityChanged(int opacity) {
        }
    }
}
