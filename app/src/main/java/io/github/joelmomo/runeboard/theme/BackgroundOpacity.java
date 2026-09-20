package io.github.joelmomo.runeboard.theme;

public final class BackgroundOpacity {

    private static final int[] LEVELS = {255, 170, 85, 0};

    private BackgroundOpacity() {
    }

    public static int defaultValue() {
        return LEVELS[0];
    }

    public static int normalize(int value) {
        int best = LEVELS[0];
        int bestDistance = Math.abs(value - best);
        for (int level : LEVELS) {
            int distance = Math.abs(value - level);
            if (distance < bestDistance) {
                best = level;
                bestDistance = distance;
            }
        }
        return best;
    }

    public static int next(int current) {
        int normalized = normalize(current);
        for (int i = 0; i < LEVELS.length; i++) {
            if (LEVELS[i] == normalized) {
                return LEVELS[(i + 1) % LEVELS.length];
            }
        }
        return LEVELS[0];
    }

    public static int percent(int value) {
        return Math.round(normalize(value) * 100f / 255f);
    }
}
