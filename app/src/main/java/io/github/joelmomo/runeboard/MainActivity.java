package io.github.joelmomo.runeboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(20);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(16, 16, 20));

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextColor(Color.WHITE);
        title.setTextSize(28f);
        root.addView(title);

        TextView status = new TextView(this);
        status.setText(R.string.setup_description);
        status.setTextColor(Color.rgb(190, 190, 200));
        status.setTextSize(16f);
        status.setPadding(0, dp(12), 0, dp(20));
        root.addView(status);

        Button enable = new Button(this);
        enable.setText(R.string.enable_keyboard);
        enable.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        root.addView(enable, matchWidth());

        Button choose = new Button(this);
        choose.setText(R.string.choose_keyboard);
        choose.setOnClickListener(v -> {
            InputMethodManager imm = getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.showInputMethodPicker();
            }
        });
        root.addView(choose, matchWidth());

        Button controls = new Button(this);
        controls.setText(R.string.enable_physical_controls);
        controls.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(controls, matchWidth());

        TextView controlsNote = new TextView(this);
        controlsNote.setText(R.string.physical_controls_note);
        controlsNote.setTextColor(Color.rgb(160, 160, 170));
        controlsNote.setTextSize(13f);
        controlsNote.setPadding(0, dp(8), 0, 0);
        root.addView(controlsNote);

        EditText testField = new EditText(this);
        testField.setHint(R.string.test_hint);
        testField.setTextColor(Color.WHITE);
        testField.setHintTextColor(Color.GRAY);
        testField.setSingleLine(false);
        testField.setMinLines(4);
        testField.setGravity(Gravity.TOP);
        testField.setPadding(dp(12), dp(12), dp(12), dp(12));

        LinearLayout.LayoutParams editParams = matchWidth();
        editParams.topMargin = dp(20);
        root.addView(testField, editParams);

        setContentView(root);
    }

    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
