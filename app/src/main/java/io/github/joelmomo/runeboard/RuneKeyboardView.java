package io.github.joelmomo.runeboard;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import io.github.joelmomo.runeboard.controller.BindableAction;
import io.github.joelmomo.runeboard.controller.ControllerAction;
import io.github.joelmomo.runeboard.controller.ControllerBindings;
import io.github.joelmomo.runeboard.controller.ControllerKeyNames;
import io.github.joelmomo.runeboard.controller.ControllerMapper;
import io.github.joelmomo.runeboard.keyboard.KeyboardEngine;
import io.github.joelmomo.runeboard.keyboard.KeyboardKey;
import io.github.joelmomo.runeboard.keyboard.KeyboardLayout;
import io.github.joelmomo.runeboard.keyboard.KeyboardLayouts;
import io.github.joelmomo.runeboard.keyboard.KeyboardRow;
import io.github.joelmomo.runeboard.keyboard.KeyboardState;
import io.github.joelmomo.runeboard.theme.BackgroundOpacity;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;
import io.github.joelmomo.runeboard.theme.RuneThemes;

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
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<HitTarget> hitTargets = new ArrayList<>();
    private final boolean debugLogging;
    private final KeyboardEngine engine;
    private final KeyboardTheme theme;
    private final ControllerMapper controllerMapper;

    private Listener listener;
    private LinearGradient backgroundGradient;
    private long lastAxisMoveAt;

    public RuneKeyboardView(Context context) {
        this(
                context,
                RuneThemes.defaultTheme(),
                BackgroundOpacity.defaultValue(),
                new ControllerMapper(new ControllerBindings()));
    }

    public RuneKeyboardView(
            Context context,
            KeyboardTheme theme,
            int initialOpacity) {
        this(
                context,
                theme,
                initialOpacity,
                new ControllerMapper(new ControllerBindings()));
    }

    public RuneKeyboardView(
            Context context,
            KeyboardTheme theme,
            int initialOpacity,
            ControllerMapper controllerMapper) {
        super(context);
        debugLogging = (context.getApplicationInfo().flags
                & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        this.theme = theme;
        this.controllerMapper = controllerMapper;

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
                    public void onMoveWord(int direction) {
                        if (listener != null) {
                            listener.onMoveWord(direction);
                        }
                    }

                    @Override
                    public void onMinimizedChanged(boolean minimized) {
                        if (listener != null) {
                            listener.onMinimizedChanged(minimized);
                        }
                    }

                    @Override
                    public void onBackgroundOpacityChanged(int opacity) {
                        if (listener != null) {
                            listener.onBackgroundOpacityChanged(opacity);
                        }
                    }
                },
                initialOpacity);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean shouldCaptureKeyCode(int keyCode) {
        return engine.shouldCapture(controllerMapper.fromKeyCode(keyCode));
    }

    public boolean isRepeatableKeyCode(int keyCode) {
        return controllerMapper.isRepeatable(keyCode);
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
    protected void onSizeChanged(
            int width,
            int height,
            int oldWidth,
            int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        backgroundGradient = new LinearGradient(
                0f,
                0f,
                0f,
                Math.max(1, height),
                theme.backgroundTop,
                theme.backgroundBottom,
                Shader.TileMode.CLAMP);
        rebuildGeometry(width, height);
    }

    private void rebuildGeometry(int width, int height) {
        hitTargets.clear();

        KeyboardState state = engine.getState();
        if (state.isMinimized() || width <= 0 || height <= 0) {
            return;
        }

        KeyboardLayout layout = state.getLayout();
        float outer = dp(theme.outerMarginDp);
        float gap = dp(theme.keyGapDp);
        float headerHeight = dp(theme.headerHeightDp);
        float contentTop = outer + headerHeight + gap;
        float rowGaps = gap * (layout.getRowCount() - 1);
        float availableHeight = Math.max(
                1f,
                height - contentTop - outer - rowGaps);
        float heightUnit =
                availableHeight / layout.getTotalHeightWeight();

        float top = contentTop;
        for (int rowIndex = 0; rowIndex < layout.getRowCount(); rowIndex++) {
            KeyboardRow row = layout.getRow(rowIndex);
            float rowHeight = heightUnit * row.getHeightWeight();
            float widthWithoutGaps =
                    width - outer * 2 - gap * (row.size() - 1);
            float widthUnit =
                    widthWithoutGaps / row.getTotalWidthWeight();
            float x = outer + row.getLeftInsetWeight() * widthUnit;

            for (int col = 0; col < row.size(); col++) {
                KeyboardKey key = row.getKey(col);
                float keyWidth = widthUnit * key.getWeight();
                RectF rect = new RectF(
                        x,
                        top,
                        x + keyWidth,
                        top + rowHeight);
                hitTargets.add(new HitTarget(rect, rowIndex, col));
                x += keyWidth + gap;
            }

            top += rowHeight + gap;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        KeyboardState state = engine.getState();
        drawBackground(canvas, state.getOpacity());

        if (state.isMinimized()) {
            drawMinimized(canvas);
            return;
        }

        drawHeader(canvas);

        for (HitTarget target : hitTargets) {
            KeyboardKey key = state.getLayout().getKey(target.row, target.col);
            drawKey(
                    canvas,
                    target.bounds,
                    key,
                    target.row,
                    target.row == state.getSelectedRow()
                            && target.col == state.getSelectedCol());
        }
    }

    private void drawBackground(Canvas canvas, int alpha) {
        backgroundPaint.setShader(backgroundGradient);
        backgroundPaint.setAlpha(alpha);
        canvas.drawRect(0f, 0f, getWidth(), getHeight(), backgroundPaint);
        backgroundPaint.setShader(null);
        backgroundPaint.setAlpha(255);
    }

    private void drawHeader(Canvas canvas) {
        KeyboardState state = engine.getState();
        float outer = dp(theme.outerMarginDp);
        float headerHeight = dp(theme.headerHeightDp);
        float badge = dp(23);
        float badgeTop = outer + (headerHeight - badge) / 2f;
        float badgeRadius = dp(7);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.accent);
        paint.setAlpha(255);
        canvas.drawRoundRect(
                outer,
                badgeTop,
                outer + badge,
                badgeTop + badge,
                badgeRadius,
                badgeRadius,
                paint);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(13));
        paint.setColor(theme.backgroundTop);
        float badgeBaseline =
                badgeTop + badge / 2f - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText("R", outer + badge / 2f, badgeBaseline, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(theme.textPrimary);
        paint.setTextSize(dp(13));
        float titleX = outer + badge + dp(9);
        float titleBaseline = outer + dp(15);
        canvas.drawText(
                getContext().getString(R.string.header_title),
                titleX,
                titleBaseline,
                paint);

        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(theme.textSecondary);
        paint.setTextSize(dp(9));
        canvas.drawText(
                getContext().getString(R.string.header_layout),
                titleX,
                outer + dp(30),
                paint);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(theme.accent);
        paint.setTextSize(dp(10));
        canvas.drawText(
                getContext().getString(R.string.header_controller),
                getWidth() - outer,
                outer + dp(14),
                paint);

        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(theme.textSecondary);
        paint.setTextSize(dp(9));
        canvas.drawText(
                getContext().getString(
                        R.string.header_background,
                        state.getOpacityPercent()),
                getWidth() - outer,
                outer + dp(30),
                paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.accent);
        paint.setAlpha(70);
        float lineY = outer + headerHeight + dp(2);
        canvas.drawRect(outer, lineY, getWidth() - outer, lineY + dp(1), paint);
        paint.setAlpha(255);
    }

    private void drawKey(
            Canvas canvas,
            RectF rect,
            KeyboardKey key,
            int row,
            boolean selected) {
        KeyboardState state = engine.getState();
        boolean utility = key.getType() != KeyboardKey.Type.TEXT;
        boolean activeShift =
                key.getType() == KeyboardKey.Type.SHIFT && state.isShifted();
        boolean activeCaps =
                key.getType() == KeyboardKey.Type.SHIFT && state.isCapsLocked();

        int fill = utility ? theme.utilityKeyFill : theme.keyFill;
        int alpha = utility ? theme.utilityKeyAlpha : theme.keyAlpha;

        if (activeShift && !selected) {
            fill = theme.selectedFill;
            alpha = activeCaps ? 220 : 170;
        }

        if (selected) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(theme.selectedFill);
            paint.setAlpha(245);
            canvas.drawRoundRect(
                    rect,
                    dp(theme.keyRadiusDp),
                    dp(theme.keyRadiusDp),
                    paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setColor(theme.selectedStroke);
            paint.setAlpha(240);
            canvas.drawRoundRect(
                    rect.left - dp(1),
                    rect.top - dp(1),
                    rect.right + dp(1),
                    rect.bottom + dp(1),
                    dp(theme.keyRadiusDp + 1f),
                    dp(theme.keyRadiusDp + 1f),
                    paint);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(fill);
            paint.setAlpha(alpha);
            canvas.drawRoundRect(
                    rect,
                    dp(theme.keyRadiusDp),
                    dp(theme.keyRadiusDp),
                    paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(
                key.getType() == KeyboardKey.Type.TEXT
                        ? Typeface.DEFAULT_BOLD
                        : Typeface.DEFAULT);
        paint.setColor(
                activeShift && !selected
                        ? theme.textPrimary
                        : theme.textPrimary);

        float textSize;
        if (key.getType() != KeyboardKey.Type.TEXT) {
            textSize = dp(11);
        } else if (row == 0) {
            textSize = dp(15);
        } else {
            textSize = dp(19);
        }
        paint.setTextSize(textSize);

        float baseline =
                rect.centerY() - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText(
                displayLabel(key),
                rect.centerX(),
                baseline,
                paint);

        drawControllerHint(canvas, rect, key, selected);
    }

    private void drawControllerHint(
            Canvas canvas,
            RectF rect,
            KeyboardKey key,
            boolean selected) {
        BindableAction action = bindableActionFor(key);
        if (action == null) {
            return;
        }

        String hint = ControllerKeyNames.nameFor(
                controllerMapper.getBindings().getKeyCode(action));

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(dp(7));
        paint.setColor(selected ? theme.selectedStroke : theme.accent);
        paint.setAlpha(220);
        canvas.drawText(
                hint,
                rect.right - dp(7),
                rect.top + dp(11),
                paint);
        paint.setAlpha(255);
    }

    private BindableAction bindableActionFor(KeyboardKey key) {
        switch (key.getType()) {
            case SHIFT:
                return BindableAction.SHIFT;
            case SPACE:
                return BindableAction.SPACE;
            case BACKSPACE:
                return BindableAction.BACKSPACE;
            case ENTER:
                return BindableAction.ENTER;
            case MINIMIZE:
                return BindableAction.MINIMIZE;
            default:
                return null;
        }
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
                return getContext().getString(
                        state.isCapsLocked()
                                ? R.string.key_caps
                                : R.string.key_shift);
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
        float outer = dp(theme.outerMarginDp);
        float centerY = getHeight() / 2f;
        float badge = dp(24);
        float badgeTop = centerY - badge / 2f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.accent);
        paint.setAlpha(255);
        canvas.drawRoundRect(
                outer,
                badgeTop,
                outer + badge,
                badgeTop + badge,
                dp(7),
                dp(7),
                paint);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(13));
        paint.setColor(theme.backgroundTop);
        float badgeBaseline =
                centerY - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText("R", outer + badge / 2f, badgeBaseline, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(theme.textPrimary);
        paint.setTextSize(dp(13));
        canvas.drawText(
                getContext().getString(R.string.header_title),
                outer + badge + dp(9),
                centerY - dp(1),
                paint);

        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(theme.textSecondary);
        paint.setTextSize(dp(9));
        canvas.drawText(
                getContext().getString(R.string.header_layout),
                outer + badge + dp(9),
                centerY + dp(13),
                paint);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(dp(10));
        paint.setColor(theme.accent);
        String restoreButton = ControllerKeyNames.nameFor(
                controllerMapper.getBindings().getKeyCode(
                        BindableAction.CONFIRM));
        String minimizeButton = ControllerKeyNames.nameFor(
                controllerMapper.getBindings().getKeyCode(
                        BindableAction.MINIMIZE));
        canvas.drawText(
                getContext().getString(
                        R.string.minimized_restore,
                        restoreButton,
                        minimizeButton),
                getWidth() - outer,
                badgeBaseline,
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
        ControllerAction action = controllerMapper.fromKeyCode(keyCode);
        if (debugLogging) {
            Log.d(TAG, "keyCode=" + keyCode + " action=" + action);
        }
        if (!engine.shouldCapture(action)) {
            return false;
        }

        int oldOpacity = engine.getState().getOpacity();
        boolean wasMinimized = engine.getState().isMinimized();
        KeyboardEngine.Update update = engine.handle(action);
        applyUpdate(update);

        if (debugLogging) {
            KeyboardState state = engine.getState();

            if (action == ControllerAction.SHIFT) {
                Log.d(TAG, "shiftMode=" + state.getShiftMode());
            }

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
            requestLayout();
            invalidate();
        } else if (update == KeyboardEngine.Update.VISUAL) {
            invalidate();
        }
    }

    private int dp(float value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density);
    }
}
