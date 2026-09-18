package io.github.joelmomo.runeboard.suggestion;

import android.text.InputType;

public final class SuggestionPolicy {

    private SuggestionPolicy() {
    }

    public static boolean supportsInputType(int inputType) {
        if ((inputType & InputType.TYPE_MASK_CLASS)
                != InputType.TYPE_CLASS_TEXT) {
            return false;
        }

        if ((inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0) {
            return false;
        }

        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        return variation != InputType.TYPE_TEXT_VARIATION_PASSWORD
                && variation != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                && variation != InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                && variation != InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                && variation != InputType.TYPE_TEXT_VARIATION_URI;
    }
}
