package io.github.joelmomo.runeboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RuneKeyboardView extends View {

    public interface Listener {
        void onText(String text);
        void onBackspace();
        void onSpace();
        void onEnter();
        void onMoveCursor(int direction);
        void onMinimizedChanged(boolean minimized);
    }

    private enum Kind {
        TEXT, SHIFT, SPACE, BACKSPACE, ENTER, OPACITY, MINIMIZE
    }

    private static final class Key {
        final String label;
        final String text;
        final Kind kind;
        final float weight;

        Key(String label, String text, Kind kind, float weight) {
            this.label = label;
            this.text = text;
            this.kind = kind;
            this.weight = weight;
        }
    }

    private static final class HitTarget {
        final RectF bounds;
        final int row;
        final int col;

        HitTarget(RectF bounds, int row, int col) {
            this.bounds = bounds;
            this.row = row;
            this.col = col;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<List<Key>> rows = new ArrayList<>();
    private final List<HitTarget> hitTargets = new ArrayList<>();

    private Listener listener;
    private int selectedRow = 1;
    private int selectedCol = 0;
    private boolean shifted = false;
    private boolean minimized = false;
    private int opacityIndex = 0;
    private long lastAxisMoveAt = 0L;

    private final int[] opacityLevels = {255, 205, 145, 85};

    public RuneKeyboardView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        buildRows();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void buildRows() {
        rows.clear();
        rows.add(textRow("1234567890"));
        rows.add(textRow("QWERTYUIOP"));
        rows.add(textRow("ASDFGHJKL"));
        rows.add(textRow("ZXCVBNM"));

        List<Key> actions = new ArrayList<>();
        actions.add(new Key("Shift", null, Kind.SHIFT, 1.15f));
        actions.add(new Key("Space", null, Kind.SPACE, 2.7f));
        actions.add(new Key("Back", null, Kind.BACKSPACE, 1.2f));
        actions.add(new Key("Enter", null, Kind.ENTER, 1.25f));
        actions.add(new Key("Alpha", null, Kind.OPACITY, 1.0f));
        actions.add(new Key("Min", null, Kind.MINIMIZE, 1.0f));
        rows.add(actions);
    }

    private List<Key> textRow(String chars) {
        List<Key> row = new ArrayList<>();
        for (int i = 0; i < chars.length(); i++) {
            String c = String.valueOf(chars.charAt(i));
            row.add(new Key(c, c.toLowerCase(Locale.ROOT), Kind.TEXT, 1f));
        }
        return row;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        int desiredHeight;
        if (minimized) {
            desiredHeight = dp(58);
        } else {
            desiredHeight = Math.round(Math.min(screenHeight * 0.58f, width * 0.56f));
        }

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (minimized) {
            drawMinimized(canvas);
            return;
        }

        canvas.drawColor(Color.argb(opacityLevels[opacityIndex], 17, 17, 23));
        hitTargets.clear();

        float outer = dp(8);
        float gap = dp(5);
        float rowHeight = (getHeight() - outer * 2 - gap * (rows.size() - 1)) / rows.size();

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<Key> row = rows.get(rowIndex);
            float totalWeight = 0f;
            for (Key key : row) {
                totalWeight += key.weight;
            }

            float usableWidth = getWidth() - outer * 2 - gap * (row.size() - 1);
            float x = outer;
            float top = outer + rowIndex * (rowHeight + gap);

            for (int col = 0; col < row.size(); col++) {
                Key key = row.get(col);
                float keyWidth = usableWidth * key.weight / totalWeight;
                RectF rect = new RectF(x, top, x + keyWidth, top + rowHeight);
                hitTargets.add(new HitTarget(rect, rowIndex, col));
                drawKey(canvas, rect, key, rowIndex == selectedRow && col == selectedCol);
                x += keyWidth + gap;
            }
        }
    }

    private void drawKey(Canvas canvas, RectF rect, Key key, boolean selected) {
        int baseAlpha = Math.max(120, opacityLevels[opacityIndex]);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selected
                ? Color.argb(baseAlpha, 139, 92, 246)
                : Color.argb(baseAlpha, 48, 48, 61));
        canvas.drawRoundRect(rect, dp(9), dp(9), paint);

        if (selected) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(Color.WHITE);
            canvas.drawRoundRect(rect, dp(9), dp(9), paint);
        }

        String label = displayLabel(key);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(key.kind == Kind.TEXT ? dp(20) : dp(13));
        float baseline = rect.centerY() - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText(label, rect.centerX(), baseline, paint);
    }

    private String displayLabel(Key key) {
        if (key.kind == Kind.TEXT && shifted && Character.isLetter(key.text.charAt(0))) {
            return key.text.toUpperCase(Locale.ROOT);
        }
        if (key.kind == Kind.OPACITY) {
            int percent = Math.round(opacityLevels[opacityIndex] * 100f / 255f);
            return "Alpha " + percent + "%";
        }
        return key.label;
    }

    private void drawMinimized(Canvas canvas) {
        canvas.drawColor(Color.argb(205, 17, 17, 23));
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(15));
        float baseline = getHeight() / 2f - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText("RuneBoard  ·  minimized  ·  tap or A to restore",
                getWidth() / 2f, baseline, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        requestFocus();

        if (minimized) {
            setMinimized(false);
            return true;
        }

        for (HitTarget target : hitTargets) {
            if (target.bounds.contains(event.getX(), event.getY())) {
                selectedRow = target.row;
                selectedCol = target.col;
                pressSelected();
                invalidate();
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0 && handleKeyCode(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (handleMotionEvent(event)) {
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    public boolean handleKeyCode(int keyCode) {
        if (minimized) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_A
                    || keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                setMinimized(false);
                return true;
            }
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                moveSelection(-1, 0);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                moveSelection(1, 0);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                moveSelection(0, -1);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                moveSelection(0, 1);
                return true;
            case KeyEvent.KEYCODE_BUTTON_A:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                pressSelected();
                return true;
            case KeyEvent.KEYCODE_BUTTON_B:
                notifyBackspace();
                return true;
            case KeyEvent.KEYCODE_BUTTON_X:
                notifySpace();
                return true;
            case KeyEvent.KEYCODE_BUTTON_Y:
                toggleShift();
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                notifyMoveCursor(-1);
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                notifyMoveCursor(1);
                return true;
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_ENTER:
                notifyEnter();
                return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                setMinimized(!minimized);
                return true;
            default:
                return false;
        }
    }

    public boolean handleMotionEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_MOVE) {
            return false;
        }

        int source = event.getSource();
        boolean joystick = (source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
        boolean gamepad = (source & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD;
        if (!joystick && !gamepad) {
            return false;
        }

        long now = event.getEventTime();
        if (now - lastAxisMoveAt < 140L) {
            return true;
        }

        float x = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float y = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

        if (Math.abs(x) < 0.55f && Math.abs(y) < 0.55f) {
            x = event.getAxisValue(MotionEvent.AXIS_X);
            y = event.getAxisValue(MotionEvent.AXIS_Y);
        }

        if (Math.abs(x) < 0.55f && Math.abs(y) < 0.55f) {
            return false;
        }

        if (minimized) {
            return true;
        }

        if (Math.abs(x) >= Math.abs(y)) {
            moveSelection(x < 0 ? -1 : 1, 0);
        } else {
            moveSelection(0, y < 0 ? -1 : 1);
        }

        lastAxisMoveAt = now;
        return true;
    }

    private void moveSelection(int dx, int dy) {
        if (minimized) {
            return;
        }

        if (dy != 0) {
            int oldSize = rows.get(selectedRow).size();
            float relative = (selectedCol + 0.5f) / oldSize;
            selectedRow = Math.max(0, Math.min(rows.size() - 1, selectedRow + dy));
            int newSize = rows.get(selectedRow).size();
            selectedCol = Math.min(newSize - 1, (int) (relative * newSize));
        } else if (dx != 0) {
            int size = rows.get(selectedRow).size();
            selectedCol = (selectedCol + dx + size) % size;
        }

        invalidate();
    }

    private void pressSelected() {
        if (minimized) {
            setMinimized(false);
            return;
        }

        Key key = rows.get(selectedRow).get(selectedCol);
        switch (key.kind) {
            case TEXT:
                if (listener != null) {
                    String output = key.text;
                    if (shifted && Character.isLetter(output.charAt(0))) {
                        output = output.toUpperCase(Locale.ROOT);
                    }
                    listener.onText(output);
                }
                if (shifted) {
                    shifted = false;
                    invalidate();
                }
                break;
            case SHIFT:
                toggleShift();
                break;
            case SPACE:
                notifySpace();
                break;
            case BACKSPACE:
                notifyBackspace();
                break;
            case ENTER:
                notifyEnter();
                break;
            case OPACITY:
                opacityIndex = (opacityIndex + 1) % opacityLevels.length;
                invalidate();
                break;
            case MINIMIZE:
                setMinimized(true);
                break;
        }
    }

    private void toggleShift() {
        shifted = !shifted;
        invalidate();
    }

    private void setMinimized(boolean value) {
        if (minimized == value) {
            return;
        }
        minimized = value;
        requestLayout();
        invalidate();
        if (listener != null) {
            listener.onMinimizedChanged(minimized);
        }
    }

    private void notifyBackspace() {
        if (listener != null) {
            listener.onBackspace();
        }
    }

    private void notifySpace() {
        if (listener != null) {
            listener.onSpace();
        }
    }

    private void notifyEnter() {
        if (listener != null) {
            listener.onEnter();
        }
    }

    private void notifyMoveCursor(int direction) {
        if (listener != null) {
            listener.onMoveCursor(direction);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
