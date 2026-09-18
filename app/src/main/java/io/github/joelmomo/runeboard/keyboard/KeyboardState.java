package io.github.joelmomo.runeboard.keyboard;

import io.github.joelmomo.runeboard.theme.BackgroundOpacity;

public final class KeyboardState {

    public enum ShiftMode {
        OFF,
        ONCE,
        CAPS_LOCK
    }

    private final KeyboardLayout alphabetLayout;
    private final KeyboardLayout symbolLayout;
    private KeyboardLayout layout;
    private boolean symbols;
    private int selectedRow = 1;
    private int selectedCol = 0;
    private ShiftMode shiftMode = ShiftMode.OFF;
    private boolean minimized;
    private int opacity;

    public KeyboardState(KeyboardLayout layout) {
        this(
                layout,
                layout,
                BackgroundOpacity.defaultValue());
    }

    public KeyboardState(
            KeyboardLayout layout,
            int initialOpacity) {
        this(layout, layout, initialOpacity);
    }

    public KeyboardState(
            KeyboardLayout alphabetLayout,
            KeyboardLayout symbolLayout,
            int initialOpacity) {
        if (alphabetLayout == null || symbolLayout == null) {
            throw new IllegalArgumentException("Layouts cannot be null");
        }
        this.alphabetLayout = alphabetLayout;
        this.symbolLayout = symbolLayout;
        this.layout = alphabetLayout;
        opacity = BackgroundOpacity.normalize(initialOpacity);
    }

    public KeyboardLayout getLayout() {
        return layout;
    }

    public boolean isSymbols() {
        return symbols;
    }

    public boolean toggleSymbols() {
        symbols = !symbols;
        layout = symbols ? symbolLayout : alphabetLayout;

        selectModeKey();

        if (symbols) {
            shiftMode = ShiftMode.OFF;
        }
        return true;
    }

    private void selectModeKey() {
        int row = layout.getRowCount() - 1;
        KeyboardRow utilityRow = layout.getRow(row);
        for (int col = 0; col < utilityRow.size(); col++) {
            if (utilityRow.getKey(col).getType()
                    == KeyboardKey.Type.MODE) {
                selectedRow = row;
                selectedCol = col;
                return;
            }
        }

        selectedRow = Math.max(
                0,
                Math.min(selectedRow, layout.getRowCount() - 1));
        selectedCol = Math.max(
                0,
                Math.min(
                        selectedCol,
                        layout.getRow(selectedRow).size() - 1));
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

    public ShiftMode getShiftMode() {
        return shiftMode;
    }

    public boolean isShifted() {
        return shiftMode != ShiftMode.OFF;
    }

    public boolean isCapsLocked() {
        return shiftMode == ShiftMode.CAPS_LOCK;
    }

    public void advanceShiftMode() {
        if (symbols) {
            return;
        }
        switch (shiftMode) {
            case OFF:
                shiftMode = ShiftMode.ONCE;
                break;
            case ONCE:
                shiftMode = ShiftMode.CAPS_LOCK;
                break;
            case CAPS_LOCK:
            default:
                shiftMode = ShiftMode.OFF;
                break;
        }
    }

    public boolean consumeOneShotShift() {
        if (shiftMode != ShiftMode.ONCE) {
            return false;
        }
        shiftMode = ShiftMode.OFF;
        return true;
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
