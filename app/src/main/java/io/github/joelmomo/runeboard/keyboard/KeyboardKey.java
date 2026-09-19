package io.github.joelmomo.runeboard.keyboard;

public final class KeyboardKey {

    public enum Type {
        TEXT,
        SHIFT,
        MODE,
        EDIT,
        COMMAND,
        SPACE,
        BACKSPACE,
        ENTER,
        OPACITY,
        MINIMIZE
    }

    private final Type type;
    private final String text;
    private final EditorCommand command;
    private final float weight;

    private KeyboardKey(
            Type type,
            String text,
            EditorCommand command,
            float weight) {
        this.type = type;
        this.text = text;
        this.command = command;
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
        return new KeyboardKey(Type.TEXT, text, null, weight);
    }

    public static KeyboardKey action(Type type, float weight) {
        if (type == Type.TEXT || type == Type.COMMAND) {
            throw new IllegalArgumentException(
                    "Use text() or command() for this key type");
        }
        if (weight <= 0f) {
            throw new IllegalArgumentException("Key weight must be positive");
        }
        return new KeyboardKey(type, null, null, weight);
    }

    public static KeyboardKey command(
            EditorCommand command,
            String label,
            float weight) {
        if (command == null) {
            throw new IllegalArgumentException(
                    "Editor command cannot be null");
        }
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException(
                    "Editor command label cannot be empty");
        }
        if (weight <= 0f) {
            throw new IllegalArgumentException("Key weight must be positive");
        }
        return new KeyboardKey(
                Type.COMMAND,
                label,
                command,
                weight);
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public EditorCommand getCommand() {
        return command;
    }

    public float getWeight() {
        return weight;
    }
}
