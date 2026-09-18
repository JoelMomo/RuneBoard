package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KeyboardLayouts {

    private KeyboardLayouts() {
    }

    public static KeyboardLayout qwerty() {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row(textKeys("1234567890"), 0.35f, 0.35f, 0.78f));
        rows.add(row(textKeys("QWERTYUIOP"), 0f, 0f, 1f));
        rows.add(row(textKeys("ASDFGHJKL"), 0.52f, 0.52f, 1f));
        rows.add(row(textKeys("ZXCVBNM"), 1.35f, 1.35f, 1f));

        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.SHIFT, 1.15f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 2.85f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.2f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.25f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.OPACITY, 1.0f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.MINIMIZE, 1.0f));
        rows.add(row(actions, 0f, 0f, 1.08f));

        return new KeyboardLayout(rows);
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
        for (int i = 0; i < characters.length(); i++) {
            String value = String.valueOf(characters.charAt(i))
                    .toLowerCase(Locale.ROOT);
            row.add(KeyboardKey.text(value));
        }
        return row;
    }
}
