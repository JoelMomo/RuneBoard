package io.github.joelmomo.runeboard;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import io.github.joelmomo.runeboard.controller.ControllerAction;
import io.github.joelmomo.runeboard.controller.ControllerMapper;
import io.github.joelmomo.runeboard.keyboard.KeyboardEngine;
import io.github.joelmomo.runeboard.keyboard.KeyboardKey;
import io.github.joelmomo.runeboard.keyboard.KeyboardLayout;
import io.github.joelmomo.runeboard.keyboard.KeyboardLayouts;
import io.github.joelmomo.runeboard.keyboard.KeyboardState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RuneKeyboardView extends View {

    private static final String TAG = "RuneBoard";

    public interface Listener extends KeyboardEngine.Output {
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
    private final List<HitTarget> hitTargets = new ArrayList<>();
    private final boolean debugLogging;
    private final KeyboardEngine engine;

    private Listener listener;
    private long lastAxisMoveAt;

    public RuneKeyboardView(Context context) {
        super(context);
        debugLogging = (context.getApplicationInfo().flags
                & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        engine = new KeyboardEngine(
                KeyboardLayouts.qwerty(),
                new KeyboardEngine.Output() {
                    @Override
                    public void onText(String text) {
                        if (listener != null) {
                            listener.onText(text);
                        }
                    }

                    @Override
                    public void onBackspace() {
                        if (listener != null) {
                            listener.onBackspace();
                        }
                    }

                    @Override
                    public void onSpace() {
                        if (listener != null) {
                            listener.onSpace();
                        }
                    }

                    @Override
                    public void onEnter() {
                        if (listener != null) {
                            listener.onEnter();
                        }
                    }

                    @Override
                    public void onMoveCursor(int direction) {
                        if (listener != null) {
                            listener.onMoveCursor(direction);
                        }
                    }

                    @Override
                    public void onMinimizedChanged(boolean minimized) {
                        if (listener != null) {
                            listener.onMinimizedChanged(minimized);
                        }
                    }
                });

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean shouldCaptureKeyCode(int keyCode) {
        return engine.shouldCapture(ControllerMapper.fromKeyCode(keyCode));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        int desiredHeight;
        if (engine.getState().isMinimized()) {
            desiredHeight = dp(58);
        } else {
            desiredHeight = Math.round(
                    Math.min(screenHeight * 0.58f, width * 0.56f));
        }

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        rebuildGeometry(width, height);
    }

    private void rebuildGeometry(int width, int height) {
        hitTargets.clear();

        KeyboardState state = engine.getState();
        if (state.isMinimized() || width <= 0 || height <= 0) {
            return;
        }

        KeyboardLayout layout = state.getLayout();
        float outer = dp(8);
        float gap = dp(5);
        float rowHeight = (height - outer * 2
                - gap * (layout.getRowCount() - 1))
                / layout.getRowCount();

        for (int rowIndex = 0; rowIndex < layout.getRowCount(); rowIndex++) {
            List<KeyboardKey> row = layout.getRow(rowIndex);
            float totalWeight = 0f;
            for (KeyboardKey key : row) {
                totalWeight += key.getWeight();
            }

            float usableWidth = width - outer * 2 - gap * (row.size() - 1);
            float x = outer;
            float top = outer + rowIndex * (rowHeight + gap);

            for (int col = 0; col < row.size(); col++) {
                KeyboardKey key = row.get(col);
                float keyWidth = usableWidth * key.getWeight() / totalWeight;
                RectF rect = new RectF(
                        x,
                        top,
                        x + keyWidth,
                        top + rowHeight);
                hitTargets.add(new HitTarget(rect, rowIndex, col));
                x += keyWidth + gap;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        KeyboardState state = engine.getState();
        if (state.isMinimized()) {
            drawMinimized(canvas);
            return;
        }

        canvas.drawColor(Color.argb(state.getOpacity(), 17, 17, 23));

        for (HitTarget target : hitTargets) {
            KeyboardKey key = state.getLayout().getKey(target.row, target.col);
            drawKey(
                    canvas,
                    target.bounds,
                    key,
                    target.row == state.getSelectedRow()
                            && target.col == state.getSelectedCol());
        }
    }

    private void drawKey(
            Canvas canvas,
            RectF rect,
            KeyboardKey key,
            boolean selected) {
        KeyboardState state = engine.getState();
        int baseAlpha = Math.max(120, state.getOpacity());

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

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(
                key.getType() == KeyboardKey.Type.TEXT ? dp(20) : dp(13));
        float baseline =
                rect.centerY() - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText(
                displayLabel(key),
                rect.centerX(),
                baseline,
                paint);
    }

    private String displayLabel(KeyboardKey key) {
        KeyboardState state = engine.getState();

        if (key.getType() == KeyboardKey.Type.TEXT) {
            String text = key.getText();
            if (state.isShifted() && Character.isLetter(text.charAt(0))) {
                return text.toUpperCase(Locale.ROOT);
            }
            return text;
        }

        switch (key.getType()) {
            case SHIFT:
                return getContext().getString(R.string.key_shift);
            case SPACE:
                return getContext().getString(R.string.key_space);
            case BACKSPACE:
                return getContext().getString(R.string.key_backspace);
            case ENTER:
                return getContext().getString(R.string.key_enter);
            case OPACITY:
                return getContext().getString(
                        R.string.key_opacity,
                        state.getOpacityPercent());
            case MINIMIZE:
                return getContext().getString(R.string.key_minimize);
            default:
                return "";
        }
    }

    private void drawMinimized(Canvas canvas) {
        canvas.drawColor(Color.argb(205, 17, 17, 23));
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(15));
        float baseline =
                getHeight() / 2f - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText(
                getContext().getString(R.string.minimized_label),
                getWidth() / 2f,
                baseline,
                paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        requestFocus();
        performClick();

        if (engine.getState().isMinimized()) {
            applyUpdate(engine.pressSelected());
            return true;
        }

        for (HitTarget target : hitTargets) {
            if (target.bounds.contains(event.getX(), event.getY())) {
                KeyboardEngine.Update selectionUpdate =
                        engine.select(target.row, target.col);
                KeyboardEngine.Update pressUpdate = engine.pressSelected();
                applyUpdate(merge(selectionUpdate, pressUpdate));
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
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
        ControllerAction action = ControllerMapper.fromKeyCode(keyCode);
        if (!engine.shouldCapture(action)) {
            return false;
        }

        int oldOpacity = engine.getState().getOpacity();
        boolean wasMinimized = engine.getState().isMinimized();
        KeyboardEngine.Update update = engine.handle(action);
        applyUpdate(update);

        if (debugLogging) {
            KeyboardState state = engine.getState();

            if (action == ControllerAction.MOVE_LEFT
                    || action == ControllerAction.MOVE_RIGHT
                    || action == ControllerAction.MOVE_UP
                    || action == ControllerAction.MOVE_DOWN) {
                Log.d(TAG, "selection row=" + state.getSelectedRow()
                        + " col=" + state.getSelectedCol()
                        + " type=" + state.getSelectedKey().getType());
            }

            if (oldOpacity != state.getOpacity()) {
                Log.d(TAG, "opacity=" + state.getOpacity());
            }

            if (wasMinimized != state.isMinimized()) {
                Log.d(TAG, "minimized=" + state.isMinimized()
                        + " measured=" + getWidth() + "x" + getHeight());
            }
        }

        return true;
    }

    public boolean handleMotionEvent(MotionEvent event) {
        if (event.getActionMasked() != MotionEvent.ACTION_MOVE
                || engine.getState().isMinimized()) {
            return false;
        }

        int source = event.getSource();
        boolean joystick =
                (source & InputDevice.SOURCE_JOYSTICK)
                        == InputDevice.SOURCE_JOYSTICK;
        boolean gamepad =
                (source & InputDevice.SOURCE_GAMEPAD)
                        == InputDevice.SOURCE_GAMEPAD;

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

        ControllerAction action;
        if (Math.abs(x) >= Math.abs(y)) {
            action = x < 0
                    ? ControllerAction.MOVE_LEFT
                    : ControllerAction.MOVE_RIGHT;
        } else {
            action = y < 0
                    ? ControllerAction.MOVE_UP
                    : ControllerAction.MOVE_DOWN;
        }

        applyUpdate(engine.handle(action));
        lastAxisMoveAt = now;
        return true;
    }

    private KeyboardEngine.Update merge(
            KeyboardEngine.Update first,
            KeyboardEngine.Update second) {
        if (first == KeyboardEngine.Update.LAYOUT
                || second == KeyboardEngine.Update.LAYOUT) {
            return KeyboardEngine.Update.LAYOUT;
        }
        if (first == KeyboardEngine.Update.VISUAL
                || second == KeyboardEngine.Update.VISUAL) {
            return KeyboardEngine.Update.VISUAL;
        }
        return KeyboardEngine.Update.NONE;
    }

    private void applyUpdate(KeyboardEngine.Update update) {
        if (update == KeyboardEngine.Update.LAYOUT) {
            rebuildGeometry(getWidth(), getHeight());
            requestLayout();
            invalidate();
        } else if (update == KeyboardEngine.Update.VISUAL) {
            invalidate();
        }
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density);
    }
}
