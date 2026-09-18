package io.github.joelmomo.runeboard.keyboard;

public final class WordNavigator {

    private WordNavigator() {
    }

    public static int move(String text, int position, int direction) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int length = text.length();
        int cursor = Math.max(0, Math.min(position, length));

        if (direction < 0) {
            while (cursor > 0 && !isWordCharacter(text.charAt(cursor - 1))) {
                cursor--;
            }
            while (cursor > 0 && isWordCharacter(text.charAt(cursor - 1))) {
                cursor--;
            }
            return cursor;
        }

        while (cursor < length && isWordCharacter(text.charAt(cursor))) {
            cursor++;
        }
        while (cursor < length && !isWordCharacter(text.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private static boolean isWordCharacter(char value) {
        return Character.isLetterOrDigit(value)
                || value == '_'
                || value == '\'';
    }
}
