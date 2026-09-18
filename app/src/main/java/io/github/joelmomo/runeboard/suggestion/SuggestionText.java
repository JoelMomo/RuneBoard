package io.github.joelmomo.runeboard.suggestion;

import java.util.Locale;

public final class SuggestionText {

    private SuggestionText() {
    }

    public static boolean shouldAutoCorrect(
            String currentWord,
            SuggestionResult result) {
        if (currentWord == null
                || currentWord.length() < 2
                || result == null
                || result.primary() == null) {
            return false;
        }

        return currentWord.equalsIgnoreCase(result.word)
                && !result.inDictionary
                && result.looksLikeTypo
                && result.recommended
                && !currentWord.equalsIgnoreCase(result.primary());
    }

    public static String adaptCase(
            String candidate,
            String typedWord,
            Locale locale) {
        if (candidate == null || candidate.isEmpty()
                || typedWord == null || typedWord.isEmpty()) {
            return candidate;
        }

        Locale effective = locale == null ? Locale.ROOT : locale;
        if (isAllUppercase(typedWord)) {
            return candidate.toUpperCase(effective);
        }

        int firstTyped = typedWord.codePointAt(0);
        if (Character.isUpperCase(firstTyped)) {
            int firstCandidate = candidate.codePointAt(0);
            int count = Character.charCount(firstCandidate);
            String first = new String(Character.toChars(firstCandidate))
                    .toUpperCase(effective);
            return first + candidate.substring(count);
        }

        return candidate;
    }

    private static boolean isAllUppercase(String value) {
        boolean sawLetter = false;
        int offset = 0;
        while (offset < value.length()) {
            int codePoint = value.codePointAt(offset);
            if (Character.isLetter(codePoint)) {
                sawLetter = true;
                if (!Character.isUpperCase(codePoint)) {
                    return false;
                }
            }
            offset += Character.charCount(codePoint);
        }
        return sawLetter;
    }
}
