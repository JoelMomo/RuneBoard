package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KeyboardLayouts {

    private KeyboardLayouts() {
    }

    public static KeyboardLayout qwerty() {
        List<List<KeyboardKey>> rows = new ArrayList<>();
        rows.add(textRow("1234567890"));
        rows.add(textRow("QWERTYUIOP"));
        rows.add(textRow("ASDFGHJKL"));
        rows.add(textRow("ZXCVBNM"));

        List<KeyboardKey> actions = new ArrayList<>();
        actions.add(KeyboardKey.action(KeyboardKey.Type.SHIFT, 1.15f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.SPACE, 2.7f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.BACKSPACE, 1.2f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.ENTER, 1.25f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.OPACITY, 1.0f));
        actions.add(KeyboardKey.action(KeyboardKey.Type.MINIMIZE, 1.0f));
        rows.add(actions);

        return new KeyboardLayout(rows);
    }

    private static List<KeyboardKey> textRow(String characters) {
        List<KeyboardKey> row = new ArrayList<>();
        for (int i = 0; i < characters.length(); i++) {
            String value = String.valueOf(characters.charAt(i)).toLowerCase(Locale.ROOT);
            row.add(KeyboardKey.text(value));
        }
        return row;
    }
}
