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

    private static KeyboardLayout alphabetLayout(
            String top,
            String middle,
            String bottom,
            float topInset,
            float middleInset,
            float bottomInset) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(textKeys("1234567890"), 0.35f, 0.35f, 0.78f));
        rows.add(row(textKeys(top), topInset, topInset, 1f));
        rows.add(row(textKeys(middle), middleInset, middleInset, 1f));
        rows.add(row(textKeys(bottom), bottomInset, bottomInset, 1f));
        rows.add(row(alphabetActionKeys(), 0f, 0f, 1.08f));
        return new KeyboardLayout(rows);
    }

    private static KeyboardLayout symbolLayout(String extras) {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(literalKeys("1234567890"), 0.35f, 0.35f, 0.78f));
        rows.add(row(literalKeys(COMMON_SYMBOLS_TOP), 0f, 0f, 1f));
        rows.add(row(literalKeys(extras), 0f, 0f, 1f));
        rows.add(row(literalKeys(COMMON_SYMBOLS_BOTTOM), 0f, 0f, 1f));
        rows.add(row(symbolActionKeys(), 0f, 0f, 1.08f));
        return new KeyboardLayout(rows);
    }

    private static List<KeyboardKey> alphabetActionKeys() {
        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.SHIFT, 0.95f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.MODE, 0.9f));
        actions.add(KeyboardKey.text(",", 0.7f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 2.5f));
        actions.add(KeyboardKey.text(".", 0.7f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.05f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.05f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.OPACITY, 0.85f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.MINIMIZE, 0.85f));
        return actions;
    }

    private static List<KeyboardKey> symbolActionKeys() {
        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.MODE, 1.0f));
        actions.add(KeyboardKey.text(",", 0.75f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 2.7f));
        actions.add(KeyboardKey.text(".", 0.75f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.15f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.15f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.OPACITY, 0.9f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.MINIMIZE, 0.9f));
        return actions;
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
