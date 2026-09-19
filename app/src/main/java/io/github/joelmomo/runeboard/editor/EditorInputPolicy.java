package io.github.joelmomo.runeboard.editor;

import android.text.InputType;
import android.text.TextUtils;

public final class EditorInputPolicy {

    private EditorInputPolicy() {
    }

    public static boolean supportsSuggestions(int inputType) {
        if (!isTextClass(inputType)) {
            return false;
        }
        if ((inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0) {
            return false;
        }
        return !isAddressLikeOrSensitiveVariation(inputType);
    }

    public static int capitalizationModes(int inputType) {
        if (!isTextClass(inputType)
                || isAddressLikeOrSensitiveVariation(inputType)) {
            return 0;
        }

        int explicit = 0;
        if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0) {
            explicit |= TextUtils.CAP_MODE_CHARACTERS;
        }
        if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0) {
            explicit |= TextUtils.CAP_MODE_WORDS;
        }
        if ((inputType & InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0) {
            explicit |= TextUtils.CAP_MODE_SENTENCES;
        }
        if (explicit != 0) {
            return explicit;
        }

        if ((inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0) {
            return 0;
        }

        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        switch (variation) {
            case InputType.TYPE_TEXT_VARIATION_PERSON_NAME:
            case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
            case InputType.TYPE_TEXT_VARIATION_PHONETIC:
                return TextUtils.CAP_MODE_WORDS;
            case InputType.TYPE_TEXT_VARIATION_NORMAL:
            case InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT:
            case InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
            case InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE:
            case InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT:
                return TextUtils.CAP_MODE_SENTENCES;
            default:
                return 0;
        }
    }

    private static boolean isTextClass(int inputType) {
        return (inputType & InputType.TYPE_MASK_CLASS)
                == InputType.TYPE_CLASS_TEXT;
    }

    private static boolean isAddressLikeOrSensitiveVariation(
            int inputType) {
        int variation = inputType & InputType.TYPE_MASK_VARIATION;
        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                || variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                || variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                || variation == InputType.TYPE_TEXT_VARIATION_URI;
    }
}
