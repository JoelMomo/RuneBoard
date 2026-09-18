package io.github.joelmomo.runeboard.keyboard;

public final class KeyboardKey {

    public enum Type {
        TEXT,
        SHIFT,
        MODE,
        SPACE,
        BACKSPACE,
        ENTER,
        OPACITY,
        MINIMIZE
    }

    private final Type type;
    private final String text;
    private final float weight;

    private KeyboardKey(Type type, String text, float weight) {
        this.type = type;
        this.text = text;
        this.weight = weight;
    }

    public static KeyboardKey text(String text) {
        return text(text, 1f);
    }

    public static KeyboardKey text(String text, float weight) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Text key cannot be empty");
        }
        if (weight <= 0f) {
            throw new IllegalArgumentException("Key weight must be positive");
        }
        return new KeyboardKey(Type.TEXT, text, weight);
    }

    public static KeyboardKey action(Type type, float weight) {
        if (type == Type.TEXT) {
            throw new IllegalArgumentException("Use text() for text keys");
        }
        if (weight <= 0f) {
            throw new IllegalArgumentException("Key weight must be positive");
        }
        return new KeyboardKey(type, null, weight);
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public float getWeight() {
        return weight;
    }
}
