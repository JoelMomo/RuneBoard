package io.github.joelmomo.runeboard.keyboard;

import io.github.joelmomo.runeboard.theme.BackgroundOpacity;

public final class KeyboardState {

    private final KeyboardLayout layout;
    private int selectedRow = 1;
    private int selectedCol = 0;
    private boolean shifted;
    private boolean minimized;
    private int opacity;

    public KeyboardState(KeyboardLayout layout) {
        this(layout, BackgroundOpacity.defaultValue());
    }

    public KeyboardState(KeyboardLayout layout, int initialOpacity) {
        this.layout = layout;
        opacity = BackgroundOpacity.normalize(initialOpacity);
    }

    public KeyboardLayout getLayout() {
        return layout;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public KeyboardKey getSelectedKey() {
        return layout.getKey(selectedRow, selectedCol);
    }

    public void select(int row, int col) {
        if (row < 0 || row >= layout.getRowCount()) {
            throw new IllegalArgumentException("Invalid row");
        }
        if (col < 0 || col >= layout.getRow(row).size()) {
            throw new IllegalArgumentException("Invalid column");
        }
        selectedRow = row;
        selectedCol = col;
    }

    public boolean move(int dx, int dy) {
        if (minimized || (dx == 0 && dy == 0)) {
            return false;
        }

        int oldRow = selectedRow;
        int oldCol = selectedCol;

        if (dy != 0) {
            int targetRow = Math.max(
                    0,
                    Math.min(layout.getRowCount() - 1, selectedRow + dy));
            if (targetRow != selectedRow) {
                float center = normalizedCenter(
                        layout.getRow(selectedRow),
                        selectedCol);
                selectedRow = targetRow;
                selectedCol = closestColumn(
                        layout.getRow(targetRow),
                        center);
            }
        } else {
            int size = layout.getRow(selectedRow).size();
            selectedCol = (selectedCol + dx + size) % size;
        }

        return oldRow != selectedRow || oldCol != selectedCol;
    }

    private float normalizedCenter(KeyboardRow row, int column) {
        float left = row.getLeftInsetWeight();
        for (int i = 0; i < column; i++) {
            left += row.getKey(i).getWeight();
        }
        return (left + row.getKey(column).getWeight() / 2f)
                / row.getTotalWidthWeight();
    }

    private int closestColumn(KeyboardRow row, float targetCenter) {
        int best = 0;
        float bestDistance = Float.MAX_VALUE;
        for (int col = 0; col < row.size(); col++) {
            float distance =
                    Math.abs(normalizedCenter(row, col) - targetCenter);
            if (distance < bestDistance) {
                best = col;
                bestDistance = distance;
            }
        }
        return best;
    }

    public boolean isShifted() {
        return shifted;
    }

    public void toggleShift() {
        shifted = !shifted;
    }

    public void clearShift() {
        shifted = false;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public boolean setMinimized(boolean minimized) {
        if (this.minimized == minimized) {
            return false;
        }
        this.minimized = minimized;
        return true;
    }

    public int getOpacity() {
        return opacity;
    }

    public int getOpacityPercent() {
        return BackgroundOpacity.percent(opacity);
    }

    public void cycleOpacity() {
        opacity = BackgroundOpacity.next(opacity);
    }
}
