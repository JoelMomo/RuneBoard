package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KeyboardLayouts {

    private static final String COMMON_SYMBOLS_TOP = "!?;:'\"@#$%";
    private static final String COMMON_SYMBOLS_BOTTOM = "-_+=[]{}/\\";
    private static final String EN_EXTRAS = "\u0060~&*^|<>\u00a3\u20ac";
    private static final String ES_EXTRAS =
            "\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc\u00f1\u00bf\u00a1\u20ac";
    private static final String FR_EXTRAS =
            "\u00e9\u00e8\u00e0\u00e7\u00f9\u00e2\u00ea\u00ee\u00f4\u00fb";
    private static final String RU_EXTRAS =
            "\u0451\u0401\u2116\u20bd\u20ac\u00ab\u00bb\u2014\u2026?";

    static final float MOBILE_TOOLBAR_HEIGHT = 0.62f;

    private KeyboardLayouts() {
    }

    public static KeyboardLayout qwerty() {
        return englishQwerty();
    }

    public static KeyboardLayout englishQwerty() {
        return alphabetLayout(
                "QWERTYUIOP",
                "ASDFGHJKL",
                "ZXCVBNM",
                0f,
                0.52f,
                1.35f);
    }

    public static KeyboardLayout spanishQwerty() {
        return alphabetLayout(
                "QWERTYUIOP",
                "ASDFGHJKL\u00d1",
                "ZXCVBNM",
                0f,
                0.06f,
                1.35f);
    }

    public static KeyboardLayout frenchAzerty() {
        return alphabetLayout(
                "AZERTYUIOP",
                "QSDFGHJKLM",
                "WXCVBN",
                0f,
                0.06f,
                1.85f);
    }

    public static KeyboardLayout russianJcuken() {
        return alphabetLayout(
                "\u0419\u0426\u0423\u041a\u0415\u041d\u0413\u0428\u0429\u0417\u0425\u042a",
                "\u0424\u042b\u0412\u0410\u041f\u0420\u041e\u041b\u0414\u0416\u042d",
                "\u042f\u0427\u0421\u041c\u0418\u0422\u042c\u0411\u042e",
                0f,
                0.48f,
                1.35f);
    }

    public static KeyboardLayout englishSymbols() {
        return symbolLayout(EN_EXTRAS);
    }

    public static KeyboardLayout spanishSymbols() {
        return symbolLayout(ES_EXTRAS);
    }

    public static KeyboardLayout frenchSymbols() {
        return symbolLayout(FR_EXTRAS);
    }

    public static KeyboardLayout russianSymbols() {
        return symbolLayout(RU_EXTRAS);
    }

    // Phone layouts: a compact editing bar above four typing rows. The
    // Thor layouts above stay exactly as they are; these are separate
    // definitions picked by the MOBILE interface profile.

    public static KeyboardLayout mobileEnglishQwerty() {
        return mobileAlphabetLayout(
                "QWERTYUIOP",
                "ASDFGHJKL",
                "ZXCVBNM",
                0.5f,
                0f);
    }

    public static KeyboardLayout mobileSpanishQwerty() {
        return mobileAlphabetLayout(
                "QWERTYUIOP",
                "ASDFGHJKLÑ",
                "ZXCVBNM",
                0f,
                0f);
    }

    public static KeyboardLayout mobileFrenchAzerty() {
        return mobileAlphabetLayout(
                "AZERTYUIOP",
                "QSDFGHJKLM",
                "WXCVBN",
                0f,
                1f);
    }

    public static KeyboardLayout mobileRussianJcuken() {
        return mobileAlphabetLayout(
                "ЙЦУКЕНГШЩЗХЪ",
                "ФЫВАПРОЛДЖЭ",
                "ЯЧСМИТЬБЮ",
                0f,
                0f);
    }

    public static KeyboardLayout mobileEnglishSymbols() {
        return mobileSymbolLayout(EN_EXTRAS);
    }

    public static KeyboardLayout mobileSpanishSymbols() {
        return mobileSymbolLayout(ES_EXTRAS);
    }

    public static KeyboardLayout mobileFrenchSymbols() {
        return mobileSymbolLayout(FR_EXTRAS);
    }

    public static KeyboardLayout mobileRussianSymbols() {
        return mobileSymbolLayout(RU_EXTRAS);
    }

    public static KeyboardLayout editorLayout() {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(List.of(
                command(EditorCommand.SELECT_ALL, "SELECT ALL", 2.1f),
                command(EditorCommand.CUT, "CUT", 1.6f),
                command(EditorCommand.COPY, "COPY", 1.8f),
                command(EditorCommand.PASTE, "PASTE", 1.9f)),
                0.25f, 0.25f, 0.9f));
        rows.add(row(List.of(
                command(EditorCommand.UNDO, "UNDO", 2f),
                command(EditorCommand.REDO, "REDO", 2f),
                command(EditorCommand.HOME, "HOME", 2f),
                command(EditorCommand.END, "END", 2f)),
                0.25f, 0.25f, 1f));
        rows.add(row(List.of(
                command(EditorCommand.CURSOR_LEFT, "LEFT", 2f),
                command(EditorCommand.CURSOR_RIGHT, "RIGHT", 2f),
                command(EditorCommand.WORD_LEFT, "WORD LEFT", 2f),
                command(EditorCommand.WORD_RIGHT, "WORD RIGHT", 2f)),
                0.25f, 0.25f, 1f));
        rows.add(row(List.of(
                command(EditorCommand.SELECT_LEFT, "SEL LEFT", 2f),
                command(EditorCommand.SELECT_RIGHT, "SEL RIGHT", 2f),
                command(EditorCommand.SELECT_WORD_LEFT, "SEL WORD L", 2f),
                command(EditorCommand.SELECT_WORD_RIGHT, "SEL WORD R", 2f)),
                0.25f, 0.25f, 1f));
        rows.add(row(List.of(
                KeyboardKey.action(
                        KeyboardKey.Type.BACKSPACE,
                        1.7f),
                command(
                        EditorCommand.DELETE_FORWARD,
                        "DELETE",
                        1.7f),
                KeyboardKey.action(
                        KeyboardKey.Type.SPACE,
                        2.6f),
                KeyboardKey.action(
                        KeyboardKey.Type.ENTER,
                        1.7f)),
                0.25f, 0.25f, 1f));
        rows.add(row(List.of(
                KeyboardKey.action(
                        KeyboardKey.Type.EDIT,
                        1.2f),
                KeyboardKey.action(
                        KeyboardKey.Type.OPACITY,
                        1f),
                KeyboardKey.action(
                        KeyboardKey.Type.MINIMIZE,
                        1f)),
                2.2f, 2.2f, 1.08f));
        return new KeyboardLayout(rows);
    }

    private static KeyboardLayout alphabetLayout(
            String top,
            String middle,
            String bottom,
            float topInset,
            float middleInset,
            float bottomInset) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(quickEditKeys(), 0.2f, 0.2f, 0.82f));
        rows.add(row(textKeys("1234567890"), 0.35f, 0.35f, 1f));
        rows.add(row(textKeys(top), topInset, topInset, 1f));
        rows.add(row(textKeys(middle), middleInset, middleInset, 1f));
        rows.add(row(alphabetBottomKeys(bottom), 0f, 0f, 1f));
        rows.add(row(alphabetActionKeys(), 0f, 0f, 0.72f));
        return new KeyboardLayout(rows);
    }

    private static KeyboardLayout symbolLayout(String extras) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(quickEditKeys(), 0.2f, 0.2f, 0.82f));
        rows.add(row(literalKeys("1234567890"), 0.35f, 0.35f, 1f));
        rows.add(row(literalKeys(COMMON_SYMBOLS_TOP), 0f, 0f, 1f));
        rows.add(row(literalKeys(extras), 0f, 0f, 1f));
        rows.add(row(symbolBottomKeys(), 0f, 0f, 1f));
        rows.add(row(symbolActionKeys(), 0f, 0f, 0.72f));
        return new KeyboardLayout(rows);
    }

    private static KeyboardLayout mobileAlphabetLayout(
            String top,
            String middle,
            String bottom,
            float middleInset,
            float bottomInset) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(mobileToolbarKeys(), 0f, 0f, MOBILE_TOOLBAR_HEIGHT));
        rows.add(row(textKeys(top), 0f, 0f, 1f));
        rows.add(row(textKeys(middle), middleInset, middleInset, 1f));
        rows.add(row(mobileBottomKeys(textKeys(bottom)),
                bottomInset, bottomInset, 1f));
        rows.add(row(mobileActionKeys(), 0f, 0f, 1f));
        return new KeyboardLayout(rows);
    }

    // Symbols need one more row than letters, so each one is a little
    // shorter; the keyboard keeps the same total height.
    private static KeyboardLayout mobileSymbolLayout(String extras) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(mobileToolbarKeys(), 0f, 0f, MOBILE_TOOLBAR_HEIGHT));
        rows.add(row(literalKeys("1234567890"), 0f, 0f, 0.8f));
        rows.add(row(literalKeys(COMMON_SYMBOLS_TOP), 0f, 0f, 0.8f));
        rows.add(row(literalKeys(extras), 0f, 0f, 0.8f));
        List<KeyboardKey> bottom = new ArrayList<>(
                literalKeys(COMMON_SYMBOLS_BOTTOM));
        bottom.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.3f));
        rows.add(row(bottom, 0f, 0f, 0.8f));
        rows.add(row(mobileActionKeys(), 0f, 0f, 1f));
        return new KeyboardLayout(rows);
    }

    private static List<KeyboardKey> mobileBottomKeys(
            List<KeyboardKey> letters) {
        List<KeyboardKey> keys = new ArrayList<>();
        keys.add(KeyboardKey.action(KeyboardKey.Type.SHIFT, 1.5f));
        keys.addAll(letters);
        keys.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.5f));
        return keys;
    }

    private static List<KeyboardKey> mobileActionKeys() {
        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.MODE, 1.5f));
        actions.add(KeyboardKey.text(",", 1f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 5f));
        actions.add(KeyboardKey.text(".", 1f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.5f));
        return actions;
    }

    // No opacity or minimize on a phone: the keyboard covers its own
    // area and the system back gesture already dismisses it.
    private static List<KeyboardKey> mobileToolbarKeys() {
        return List.of(
                command(EditorCommand.CURSOR_LEFT, "←", 1f),
                command(EditorCommand.CURSOR_RIGHT, "→", 1f),
                command(EditorCommand.COPY, "COPY", 1f),
                command(EditorCommand.PASTE, "PASTE", 1f),
                command(EditorCommand.UNDO, "UNDO", 1f),
                command(EditorCommand.REDO, "REDO", 1f),
                KeyboardKey.action(KeyboardKey.Type.EDIT, 1f));
    }

    private static List<KeyboardKey> alphabetBottomKeys(
            String characters) {
        List<KeyboardKey> keys = new ArrayList<>();
        keys.add(KeyboardKey.action(KeyboardKey.Type.SHIFT, 1.2f));
        keys.addAll(textKeys(characters));
        keys.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.3f));
        return keys;
    }

    private static List<KeyboardKey> symbolBottomKeys() {
        List<KeyboardKey> keys = new ArrayList<>(
                literalKeys(COMMON_SYMBOLS_BOTTOM));
        keys.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.3f));
        return keys;
    }

    private static List<KeyboardKey> alphabetActionKeys() {
        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.MODE, 1.15f));
        actions.add(KeyboardKey.text(",", 0.9f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 4.4f));
        actions.add(KeyboardKey.text(".", 0.9f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.45f));
        return actions;
    }

    private static List<KeyboardKey> quickEditKeys() {
        return List.of(
                command(EditorCommand.CURSOR_LEFT, "\u2190", 0.9f),
                command(EditorCommand.CURSOR_RIGHT, "\u2192", 0.9f),
                command(EditorCommand.COPY, "COPY", 1.15f),
                command(EditorCommand.PASTE, "PASTE", 1.25f),
                command(EditorCommand.UNDO, "UNDO", 1f),
                command(EditorCommand.REDO, "REDO", 1f),
                KeyboardKey.action(KeyboardKey.Type.OPACITY, 1f),
                KeyboardKey.action(KeyboardKey.Type.MINIMIZE, 1f),
                KeyboardKey.action(KeyboardKey.Type.EDIT, 1f));
    }

    private static List<KeyboardKey> symbolActionKeys() {
        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.MODE, 1.15f));
        actions.add(KeyboardKey.text(",", 0.9f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 4.4f));
        actions.add(KeyboardKey.text(".", 0.9f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.45f));
        return actions;
    }

    private static KeyboardKey command(
            EditorCommand command,
            String label,
            float weight) {
        return KeyboardKey.command(command, label, weight);
    }

    private static KeyboardRow row(
            List<KeyboardKey> keys,
            float leftInset,
            float rightInset,
            float heightWeight) {
        return new KeyboardRow(keys, leftInset, rightInset, heightWeight);
    }

    private static List<KeyboardKey> textKeys(String characters) {
        List<KeyboardKey> row = new ArrayList<>();
        int offset = 0;
        while (offset < characters.length()) {
            int codePoint = characters.codePointAt(offset);
            String value = new String(Character.toChars(codePoint))
                    .toLowerCase(Locale.ROOT);
            row.add(KeyboardKey.text(value));
            offset += Character.charCount(codePoint);
        }
        return row;
    }

    private static List<KeyboardKey> literalKeys(String characters) {
        List<KeyboardKey> row = new ArrayList<>();
        int offset = 0;
        while (offset < characters.length()) {
            int codePoint = characters.codePointAt(offset);
            row.add(KeyboardKey.text(
                    new String(Character.toChars(codePoint))));
            offset += Character.charCount(codePoint);
        }
        return row;
    }
}
