package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KeyboardRow {

    private final List<KeyboardKey> keys;
    private final float leftInsetWeight;
    private final float rightInsetWeight;
    private final float heightWeight;

    public KeyboardRow(
            List<KeyboardKey> keys,
            float leftInsetWeight,
            float rightInsetWeight,
            float heightWeight) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("Keyboard row cannot be empty");
        }
        if (leftInsetWeight < 0f || rightInsetWeight < 0f || heightWeight <= 0f) {
            throw new IllegalArgumentException("Invalid keyboard row weights");
        }

        this.keys = Collections.unmodifiableList(new ArrayList<>(keys));
        this.leftInsetWeight = leftInsetWeight;
        this.rightInsetWeight = rightInsetWeight;
        this.heightWeight = heightWeight;
    }

    public List<KeyboardKey> getKeys() {
        return keys;
    }

    public KeyboardKey getKey(int column) {
        return keys.get(column);
    }

    public int size() {
        return keys.size();
    }

    public float getLeftInsetWeight() {
        return leftInsetWeight;
    }

    public float getRightInsetWeight() {
        return rightInsetWeight;
    }

    public float getHeightWeight() {
        return heightWeight;
    }

    public float getContentWeight() {
        float total = 0f;
        for (KeyboardKey key : keys) {
            total += key.getWeight();
        }
        return total;
    }

    public float getTotalWidthWeight() {
        return leftInsetWeight + getContentWeight() + rightInsetWeight;
    }
}
