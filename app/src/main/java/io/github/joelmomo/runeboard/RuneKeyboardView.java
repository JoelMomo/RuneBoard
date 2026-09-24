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
import android.view.WindowInsets;

import io.github.joelmomo.runeboard.controller.BindableAction;
import io.github.joelmomo.runeboard.controller.ControllerAction;
import io.github.joelmomo.runeboard.controller.ControllerBindings;
import io.github.joelmomo.runeboard.controller.ControllerKeyNames;
import io.github.joelmomo.runeboard.controller.AxisNavigationPolicy;
import io.github.joelmomo.runeboard.controller.ControllerMapper;
import io.github.joelmomo.runeboard.editor.EditorActionSpec;
import io.github.joelmomo.runeboard.keyboard.EditorCommand;
import io.github.joelmomo.runeboard.keyboard.KeyboardEngine;
import io.github.joelmomo.runeboard.keyboard.KeyboardKey;
import io.github.joelmomo.runeboard.keyboard.KeyboardLayout;
import io.github.joelmomo.runeboard.keyboard.KeyboardLayouts;
import io.github.joelmomo.runeboard.keyboard.KeyRepeatPolicy;
import io.github.joelmomo.runeboard.keyboard.KeyboardRow;
import io.github.joelmomo.runeboard.language.KeyboardProfile;
import io.github.joelmomo.runeboard.language.KeyboardProfiles;
import io.github.joelmomo.runeboard.keyboard.KeyboardState;
import io.github.joelmomo.runeboard.theme.BackgroundOpacity;
import io.github.joelmomo.runeboard.theme.KeyboardTheme;
import io.github.joelmomo.runeboard.theme.RuneThemes;

import java.util.ArrayList;
import java.util.List;

public final class RuneKeyboardView extends View {

    private static final String TAG = "RuneBoard";

    public interface Listener extends KeyboardEngine.Output {
        void onSuggestionSelected(String suggestion);
    }

    private static final class SuggestionTarget {
        final RectF bounds;
        final String suggestion;

        SuggestionTarget(RectF bounds, String suggestion) {
            this.bounds = bounds;
            this.suggestion = suggestion;
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
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<HitTarget> hitTargets = new ArrayList<>();
    private final List<SuggestionTarget> suggestionTargets = new ArrayList<>();
    private final List<String> suggestions = new ArrayList<>();
    private final RectF languageTarget = new RectF();
    private final boolean debugLogging;
    private final KeyboardEngine engine;
    private final KeyboardTheme theme;
    private final ControllerMapper controllerMapper;
    private final AxisNavigationPolicy axisNavigationPolicy =
            new AxisNavigationPolicy();
    private final KeyboardProfile profile;

    private EditorActionSpec editorAction = EditorActionSpec.enter();
    private Listener listener;
    private LinearGradient backgroundGradient;
    private boolean suggestionsRecommended;
    private final Runnable touchRepeatRunnable;
    private final RectF touchRepeatBounds = new RectF();
    private int touchRepeatRow = -1;
    private int touchRepeatCol = -1;
    private boolean touchRepeatActive;

    public RuneKeyboardView(Context context) {
        this(
                context,
                KeyboardProfiles.defaultProfile(),
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
                KeyboardProfiles.defaultProfile(),
                theme,
                initialOpacity,
                new ControllerMapper(new ControllerBindings()));
    }

    public RuneKeyboardView(
            Context context,
            KeyboardTheme theme,
            int initialOpacity,
            ControllerMapper controllerMapper) {
        this(
                context,
                KeyboardProfiles.defaultProfile(),
                theme,
                initialOpacity,
                controllerMapper);
    }

    public RuneKeyboardView(
            Context context,
            KeyboardProfile profile,
            KeyboardTheme theme,
            int initialOpacity,
            ControllerMapper controllerMapper) {
        super(context);
        debugLogging = (context.getApplicationInfo().flags
                & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        this.profile = profile;
        this.theme = theme;
        this.controllerMapper = controllerMapper;

        engine = new KeyboardEngine(
                profile.layout,
                profile.symbolLayout,
                KeyboardLayouts.editorLayout(),
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
                    public void onNextLanguage() {
                        if (listener != null) {
                            listener.onNextLanguage();
                        }
                    }

                    @Override
                    public void onAcceptSuggestion() {
                        if (listener != null) {
                            listener.onAcceptSuggestion();
                        }
                    }

                    @Override
                    public void onEditorCommand(EditorCommand command) {
                        if (listener != null) {
                            listener.onEditorCommand(command);
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
                initialOpacity,
                profile.locale);

        touchRepeatRunnable = this::repeatTouchKey;
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setSuggestions(List<String> values, boolean recommended) {
        suggestions.clear();
        suggestionTargets.clear();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    suggestions.add(value);
                    if (suggestions.size() == 3) {
                        break;
                    }
                }
            }
        }
        suggestionsRecommended = recommended;
        invalidate();
    }

    public void clearSuggestions() {
        setSuggestions(null, false);
    }

    public void setEditorAction(EditorActionSpec action) {
        editorAction = action == null
                ? EditorActionSpec.enter()
                : action;
        if (debugLogging) {
            Log.d(
                    TAG,
                    "editorAction="
                            + editorAction.kind()
                            + " actionId="
                            + editorAction.actionId());
        }
        invalidate();
    }

    public void setAutoCapitalization(boolean enabled) {
        applyUpdate(engine.setAutoShifted(enabled));
        if (debugLogging) {
            Log.d(
                    TAG,
                    "autoCapitalization="
                            + enabled
                            + " shiftMode="
                            + engine.getState().getShiftMode());
        }
    }

    public void resetShiftMode() {
        applyUpdate(engine.resetShiftMode());
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
        int availableHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredHeight = engine.getState().isMinimized()
                ? dp(58)
                : availableHeight;

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
        languageTarget.setEmpty();

        KeyboardState state = engine.getState();
        if (state.isMinimized() || width <= 0 || height <= 0) {
            return;
        }

        KeyboardLayout layout = state.getLayout();
        float outer = dp(theme.outerMarginDp);
        float gap = dp(theme.keyGapDp);
        float headerHeight = dp(theme.headerHeightDp);
        languageTarget.set(
                outer,
                outer,
                Math.min(width * 0.46f, outer + dp(285)),
                outer + headerHeight);
        float contentTop = outer + headerHeight + gap;
        float rowGaps = gap * (layout.getRowCount() - 1);
        // Keep the utility row at the top, then shift the typing block
        // downward by exactly the old bottom margin. This preserves key
        // heights while making the thumb row finish flush with the bottom.
        float typingBlockOffset = outer;
        float availableHeight = Math.max(
                1f,
                height - contentTop - typingBlockOffset - rowGaps);
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
            if (rowIndex == 0 && layout.getRowCount() > 1) {
                top += typingBlockOffset;
            }
        }

    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        WindowInsets applied = super.onApplyWindowInsets(insets);
        if (getWidth() > 0 && getHeight() > 0) {
            rebuildGeometry(getWidth(), getHeight());
            invalidate();
        }
        return applied;
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
        suggestionTargets.clear();
        KeyboardState state = engine.getState();
        float outer = dp(theme.outerMarginDp);
        float headerHeight = dp(theme.headerHeightDp);
        drawHeaderSurface(canvas, outer, headerHeight);

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
                profile.shortLabel
                        + " / "
                        + (state.isEditing()
                                ? "EDIT"
                                : state.isSymbols()
                                        ? "SYM"
                                        : profile.layoutName)
                        + "  "
                        + ControllerKeyNames.nameFor(
                                controllerMapper.getBindings().getKeyCode(
                                        BindableAction.LANGUAGE_NEXT)),
                titleX,
                outer + dp(30),
                paint);

        if (state.isEditing() || suggestions.isEmpty()) {
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
        } else {
            drawSuggestions(canvas, outer, headerHeight);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.accent);
        paint.setAlpha(32);
        float lineY = outer + headerHeight + dp(2);
        canvas.drawRect(outer, lineY, getWidth() - outer, lineY + dp(1), paint);
        paint.setAlpha(255);
    }

    private void drawHeaderSurface(
            Canvas canvas,
            float outer,
            float headerHeight) {
        RectF surface = new RectF(
                outer,
                outer,
                getWidth() - outer,
                outer + headerHeight);
        float radius = dp(Math.min(theme.keyRadiusDp, 12f));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(theme.utilityKeyFill);
        paint.setAlpha(Math.min(theme.utilityKeyAlpha, 168));
        canvas.drawRoundRect(surface, radius, radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(theme.accent);
        paint.setAlpha(38);
        canvas.drawRoundRect(surface, radius, radius, paint);
        paint.setAlpha(255);
    }

    private void drawSuggestions(
            Canvas canvas,
            float outer,
            float headerHeight) {
        int count = Math.min(3, suggestions.size());
        float left = Math.max(languageTarget.right + dp(10), getWidth() * 0.48f);
        float right = getWidth() - outer;
        float gap = dp(5);
        float width = (right - left - gap * (count - 1)) / count;
        float top = outer + dp(3);
        float bottom = outer + headerHeight - dp(3);

        for (int index = 0; index < count; index++) {
            float chipLeft = left + index * (width + gap);
            RectF bounds = new RectF(chipLeft, top, chipLeft + width, bottom);
            String suggestion = suggestions.get(index);
            suggestionTargets.add(new SuggestionTarget(bounds, suggestion));

            boolean recommended = index == 0 && suggestionsRecommended;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(recommended
                    ? theme.selectedFill
                    : theme.utilityKeyFill);
            paint.setAlpha(recommended ? 245 : 210);
            canvas.drawRoundRect(bounds, dp(8), dp(8), paint);
            paint.setAlpha(255);

            if (index == 0) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1));
                paint.setColor(theme.accent);
                canvas.drawRoundRect(bounds, dp(8), dp(8), paint);
            }

            String label = suggestion;
            if (index == 0) {
                label += "  " + ControllerKeyNames.nameFor(
                        controllerMapper.getBindings().getKeyCode(
                                BindableAction.ACCEPT_SUGGESTION));
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(index == 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(9));
            paint.setColor(recommended
                    ? theme.selectedContent
                    : theme.textPrimary);
            String fitted = fitText(label, Math.max(1f, width - dp(12)));
            float baseline = bounds.centerY()
                    - (paint.ascent() + paint.descent()) / 2f;
            canvas.drawText(fitted, bounds.centerX(), baseline, paint);
        }
    }

    private String fitText(String value, float maxWidth) {
        if (paint.measureText(value) <= maxWidth) {
            return value;
        }
        int count = paint.breakText(value, true, maxWidth, null);
        int keep = Math.max(1, count - 3);
        return value.substring(0, Math.min(keep, value.length())) + "...";
    }

    private void drawKey(
            Canvas canvas,
            RectF rect,
            KeyboardKey key,
            int row,
            boolean selected) {
        KeyboardState state = engine.getState();
        boolean primaryTyping =
                key.getType() == KeyboardKey.Type.TEXT
                        || key.getType() == KeyboardKey.Type.SPACE;
        boolean commandRow = row == 0 && !state.isEditing();
        boolean activeShift =
                key.getType() == KeyboardKey.Type.SHIFT && state.isShifted();
        boolean activeCaps =
                key.getType() == KeyboardKey.Type.SHIFT && state.isCapsLocked();

        int fill = primaryTyping ? theme.keyFill : theme.utilityKeyFill;
        int alpha = primaryTyping ? theme.keyAlpha : theme.utilityKeyAlpha;
        if (commandRow && !selected) {
            alpha = Math.min(alpha, 176);
        }

        if (activeShift && !selected) {
            fill = theme.selectedFill;
            alpha = activeCaps ? 220 : 170;
        }

        if (selected) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(3));
            paint.setColor(theme.selectedStroke);
            paint.setAlpha(64);
            canvas.drawRoundRect(
                    rect.left - dp(2),
                    rect.top - dp(2),
                    rect.right + dp(2),
                    rect.bottom + dp(2),
                    dp(theme.keyRadiusDp + 2f),
                    dp(theme.keyRadiusDp + 2f),
                    paint);

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

            if (primaryTyping) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1));
                paint.setColor(theme.textSecondary);
                paint.setAlpha(key.getType() == KeyboardKey.Type.SPACE ? 52 : 34);
                canvas.drawRoundRect(
                        rect,
                        dp(theme.keyRadiusDp),
                        dp(theme.keyRadiusDp),
                        paint);
            }
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(
                primaryTyping
                        ? Typeface.DEFAULT_BOLD
                        : Typeface.DEFAULT);
        boolean emphasized = selected || activeShift;
        paint.setColor(
                emphasized
                        ? theme.selectedContent
                        : commandRow
                                ? theme.textSecondary
                                : theme.textPrimary);
        paint.setAlpha(commandRow && !emphasized ? 230 : 255);

        float textSize;
        if (key.getType() == KeyboardKey.Type.BACKSPACE) {
            textSize = dp(27);
        } else if (key.getType() == KeyboardKey.Type.ENTER) {
            textSize = dp(22);
        } else if (key.getType() == KeyboardKey.Type.SPACE) {
            textSize = dp(12);
        } else if (key.getType() != KeyboardKey.Type.TEXT) {
            textSize = dp(11);
        } else {
            textSize = dp(19);
        }
        paint.setTextSize(textSize);

        float baseline =
                rect.centerY() - (paint.ascent() + paint.descent()) / 2f;
        String label = displayLabel(key);
        if (key.getType() != KeyboardKey.Type.TEXT) {
            label = fitText(
                    label,
                    Math.max(1f, rect.width() - dp(10)));
        }
        canvas.drawText(
                label,
                rect.centerX(),
                baseline,
                paint);
        paint.setAlpha(255);

        drawControllerHint(canvas, rect, key, row, selected);
    }

    private void drawControllerHint(
            Canvas canvas,
            RectF rect,
            KeyboardKey key,
            int row,
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
        boolean activeShift =
                key.getType() == KeyboardKey.Type.SHIFT
                        && engine.getState().isShifted();
        boolean emphasized = selected || activeShift;
        paint.setColor(
                emphasized
                        ? theme.selectedContent
                        : theme.textSecondary);
        boolean commandRow = row == 0 && !engine.getState().isEditing();
        paint.setAlpha(
                emphasized
                        ? 255
                        : commandRow ? 225 : 230);
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
            if (state.isShifted()
                    && !state.isSymbols()
                    && !state.isEditing()
                    && Character.isLetter(text.charAt(0))) {
                return text.toUpperCase(profile.locale);
            }
            return text;
        }

        switch (key.getType()) {
            case SHIFT:
                return getContext().getString(
                        state.isCapsLocked()
                                ? R.string.key_caps
                                : R.string.key_shift);
            case MODE:
                return getContext().getString(
                        state.isSymbols()
                                ? R.string.key_letters
                                : R.string.key_symbols);
            case EDIT:
                return getContext().getString(
                        state.isEditing()
                                ? R.string.key_letters
                                : R.string.key_edit);
            case COMMAND:
                return key.getText();
            case SPACE:
                return getContext().getString(R.string.key_space);
            case BACKSPACE:
                return "⌫";
            case ENTER:
                return "↵";
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
                profile.shortLabel + " / " + profile.layoutName,
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
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            cancelTouchRepeat();
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (touchRepeatActive
                    && !touchRepeatBounds.contains(
                            event.getX(),
                            event.getY())) {
                cancelTouchRepeat();
            }
            return true;
        }

        if (action != MotionEvent.ACTION_DOWN) {
            return true;
        }

        cancelTouchRepeat();
        requestFocus();
        performClick();

        if (engine.getState().isMinimized()) {
            applyUpdate(engine.pressSelected());
            return true;
        }

        for (SuggestionTarget target : suggestionTargets) {
            if (target.bounds.contains(event.getX(), event.getY())) {
                if (listener != null) {
                    listener.onSuggestionSelected(target.suggestion);
                }
                return true;
            }
        }

        if (languageTarget.contains(event.getX(), event.getY())) {
            applyUpdate(engine.handle(ControllerAction.LANGUAGE_NEXT));
            return true;
        }

        for (HitTarget target : hitTargets) {
            if (target.bounds.contains(event.getX(), event.getY())) {
                KeyboardEngine.Update selectionUpdate =
                        engine.select(target.row, target.col);
                KeyboardKey selectedKey =
                        engine.getState().getSelectedKey();
                KeyboardEngine.Update pressUpdate =
                        engine.pressSelected();
                applyUpdate(merge(selectionUpdate, pressUpdate));
                if (KeyRepeatPolicy.isTouchKeyRepeatable(selectedKey)) {
                    startTouchRepeat(target);
                }
                return true;
            }
        }

        return true;
    }

    private void startTouchRepeat(HitTarget target) {
        touchRepeatActive = true;
        touchRepeatRow = target.row;
        touchRepeatCol = target.col;
        touchRepeatBounds.set(target.bounds);
        postDelayed(
                touchRepeatRunnable,
                KeyRepeatPolicy.TOUCH_INITIAL_DELAY_MS);
    }

    private void repeatTouchKey() {
        if (!touchRepeatActive) {
            return;
        }

        KeyboardEngine.Update selectionUpdate =
                engine.select(touchRepeatRow, touchRepeatCol);
        KeyboardKey selectedKey = engine.getState().getSelectedKey();
        if (!KeyRepeatPolicy.isTouchKeyRepeatable(selectedKey)) {
            cancelTouchRepeat();
            return;
        }

        KeyboardEngine.Update pressUpdate = engine.pressSelected();
        applyUpdate(merge(selectionUpdate, pressUpdate));
        postDelayed(
                touchRepeatRunnable,
                KeyRepeatPolicy.TOUCH_INTERVAL_MS);
    }

    private void cancelTouchRepeat() {
        touchRepeatActive = false;
        touchRepeatRow = -1;
        touchRepeatCol = -1;
        touchRepeatBounds.setEmpty();
        removeCallbacks(touchRepeatRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelTouchRepeat();
        axisNavigationPolicy.reset();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (shouldCaptureKeyCode(keyCode)) {
            if (event.getRepeatCount() == 0
                    || isRepeatableKeyCode(keyCode)) {
                handleKeyCode(keyCode);
            }
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

    public boolean shouldSuppressSyntheticDpad(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT
                && keyCode != KeyEvent.KEYCODE_DPAD_RIGHT
                && keyCode != KeyEvent.KEYCODE_DPAD_UP
                && keyCode != KeyEvent.KEYCODE_DPAD_DOWN) {
            return false;
        }
        return axisNavigationPolicy.shouldSuppressSyntheticDpad(
                event.getEventTime());
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

        float x = event.getAxisValue(MotionEvent.AXIS_X);
        float y = event.getAxisValue(MotionEvent.AXIS_Y);
        ControllerAction action = axisNavigationPolicy.onSample(
                x,
                y,
                event.getEventTime());

        if (action == null) {
            return false;
        }

        applyUpdate(engine.handle(action));
        if (debugLogging) {
            KeyboardState state = engine.getState();
            Log.d(TAG, "axisSelection x=" + x
                    + " y=" + y
                    + " row=" + state.getSelectedRow()
                    + " col=" + state.getSelectedCol()
                    + " type=" + state.getSelectedKey().getType());
        }
        return true;
    }

    private KeyboardEngine.Update merge(
            KeyboardEngine.Update first,
            KeyboardEngine.Update second) {
        if (first == KeyboardEngine.Update.LAYOUT
                || second == KeyboardEngine.Update.LAYOUT) {
            return KeyboardEngine.Update.LAYOUT;
        }
        if (first == KeyboardEngine.Update.GEOMETRY
                || second == KeyboardEngine.Update.GEOMETRY) {
            return KeyboardEngine.Update.GEOMETRY;
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
        } else if (update == KeyboardEngine.Update.GEOMETRY) {
            if (engine.getState().isEditing()) {
                suggestions.clear();
                suggestionTargets.clear();
            }
            if (getWidth() > 0 && getHeight() > 0) {
                rebuildGeometry(getWidth(), getHeight());
            }
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
