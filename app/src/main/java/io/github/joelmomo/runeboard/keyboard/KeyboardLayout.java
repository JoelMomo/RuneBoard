package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KeyboardLayout {

    private final List<List<KeyboardKey>> rows;

    public KeyboardLayout(List<List<KeyboardKey>> rows) {
        List<List<KeyboardKey>> copy = new ArrayList<>();
        for (List<KeyboardKey> row : rows) {
            if (row.isEmpty()) {
                throw new IllegalArgumentException("Keyboard rows cannot be empty");
            }
            copy.add(Collections.unmodifiableList(new ArrayList<>(row)));
        }
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("Keyboard layout cannot be empty");
        }
        this.rows = Collections.unmodifiableList(copy);
    }

    public int getRowCount() {
        return rows.size();
    }

    public List<KeyboardKey> getRow(int row) {
        return rows.get(row);
    }

    public KeyboardKey getKey(int row, int col) {
        return rows.get(row).get(col);
    }
}
