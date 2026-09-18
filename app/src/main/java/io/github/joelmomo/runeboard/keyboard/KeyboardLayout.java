package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KeyboardLayout {

    private final List<KeyboardRow> rows;

    public KeyboardLayout(List<KeyboardRow> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Keyboard layout cannot be empty");
        }
        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
    }

    public int getRowCount() {
        return rows.size();
    }

    public KeyboardRow getRow(int row) {
        return rows.get(row);
    }

    public KeyboardKey getKey(int row, int col) {
        return rows.get(row).getKey(col);
    }

    public float getTotalHeightWeight() {
        float total = 0f;
        for (KeyboardRow row : rows) {
            total += row.getHeightWeight();
        }
        return total;
    }
}
