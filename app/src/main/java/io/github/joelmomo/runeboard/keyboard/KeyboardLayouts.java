package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KeyboardLayouts {

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
                "ASDFGHJKLÑ",
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
                "ЙЦУКЕНГШЩЗХЪ",
                "ФЫВАПРОЛДЖЭ",
                "ЯЧСМИТЬБЮ",
                0f,
                0.48f,
                1.35f);
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
        rows.add(row(actionKeys(), 0f, 0f, 1.08f));
        return new KeyboardLayout(rows);
    }

    private static List<KeyboardKey> actionKeys() {
        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.SHIFT, 1.15f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 2.85f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.2f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.25f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.OPACITY, 1.0f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.MINIMIZE, 1.0f));
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
}
