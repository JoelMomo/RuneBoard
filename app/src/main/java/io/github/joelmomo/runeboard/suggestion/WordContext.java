package io.github.joelmomo.runeboard.suggestion;

public final class WordContext {

    public final String word;
    public final int length;

    private WordContext(String word) {
        this.word = word;
        this.length = word.length();
    }

    public static WordContext trailing(CharSequence text) {
        if (text == null || text.length() == 0) {
            return new WordContext("");
        }

        int end = text.length();
        int start = end;
        while (start > 0) {
            int codePoint = Character.codePointBefore(text, start);
            if (!isWordCodePoint(codePoint)) {
                break;
            }
            start -= Character.charCount(codePoint);
        }

        return new WordContext(text.subSequence(start, end).toString());
    }

    private static boolean isWordCodePoint(int codePoint) {
        return Character.isLetterOrDigit(codePoint)
                || codePoint == '_'
                || codePoint == 0x0027
                || codePoint == 0x2019;
    }
}
