package io.github.joelmomo.runeboard.keyboard;

public final class SelectionController {

    public static final class Range {
        public final int anchor;
        public final int caret;

        public Range(int anchor, int caret) {
            this.anchor = anchor;
            this.caret = caret;
        }
    }

    private int anchor = -1;
    private int caret = -1;

    public void reset() {
        anchor = -1;
        caret = -1;
    }

    public Range extend(
            String text,
            int selectionStart,
            int selectionEnd,
            int direction,
            boolean byWord) {
        String value = text == null ? "" : text;
        int length = value.length();
        int start = clamp(selectionStart, length);
        int end = clamp(selectionEnd, length);

        if (!matchesCurrent(start, end)) {
            beginFromSelection(start, end, direction);
        }

        int nextCaret;
        if (byWord) {
            nextCaret = WordNavigator.move(
                    value,
                    caret,
                    direction);
        } else {
            nextCaret = moveCodePoint(
                    value,
                    caret,
                    direction);
        }

        caret = nextCaret;
        return new Range(anchor, caret);
    }

    private void beginFromSelection(
            int start,
            int end,
            int direction) {
        if (start == end) {
            anchor = start;
            caret = end;
            return;
        }

        int left = Math.min(start, end);
        int right = Math.max(start, end);
        if (direction < 0) {
            anchor = right;
            caret = left;
        } else {
            anchor = left;
            caret = right;
        }
    }

    private boolean matchesCurrent(int start, int end) {
        if (anchor < 0 || caret < 0) {
            return false;
        }
        return (anchor == start && caret == end)
                || (anchor == end && caret == start);
    }

    private int moveCodePoint(
            String text,
            int position,
            int direction) {
        if (direction < 0) {
            if (position <= 0) {
                return 0;
            }
            return Character.offsetByCodePoints(
                    text,
                    position,
                    -1);
        }

        if (position >= text.length()) {
            return text.length();
        }
        return Character.offsetByCodePoints(
                text,
                position,
                1);
    }

    private int clamp(int value, int length) {
        return Math.max(0, Math.min(value, length));
    }
}
