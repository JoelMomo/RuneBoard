package io.github.joelmomo.runeboard.editor;

import android.view.inputmethod.EditorInfo;

public final class EditorActionResolver {

    private EditorActionResolver() {
    }

    public static EditorActionSpec resolve(
            int imeOptions,
            int actionId,
            CharSequence actionLabel) {
        if ((imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            return EditorActionSpec.enter();
        }

        if (actionId > 0 && actionLabel != null) {
            String label = actionLabel.toString().trim();
            if (!label.isEmpty()) {
                return EditorActionSpec.custom(actionId, label);
            }
        }

        int action = imeOptions & EditorInfo.IME_MASK_ACTION;
        switch (action) {
            case EditorInfo.IME_ACTION_GO:
                return EditorActionSpec.standard(
                        EditorActionSpec.Kind.GO,
                        EditorInfo.IME_ACTION_GO);
            case EditorInfo.IME_ACTION_SEARCH:
                return EditorActionSpec.standard(
                        EditorActionSpec.Kind.SEARCH,
                        EditorInfo.IME_ACTION_SEARCH);
            case EditorInfo.IME_ACTION_SEND:
                return EditorActionSpec.standard(
                        EditorActionSpec.Kind.SEND,
                        EditorInfo.IME_ACTION_SEND);
            case EditorInfo.IME_ACTION_NEXT:
                return EditorActionSpec.standard(
                        EditorActionSpec.Kind.NEXT,
                        EditorInfo.IME_ACTION_NEXT);
            case EditorInfo.IME_ACTION_DONE:
                return EditorActionSpec.standard(
                        EditorActionSpec.Kind.DONE,
                        EditorInfo.IME_ACTION_DONE);
            case EditorInfo.IME_ACTION_PREVIOUS:
                return EditorActionSpec.standard(
                        EditorActionSpec.Kind.PREVIOUS,
                        EditorInfo.IME_ACTION_PREVIOUS);
            default:
                return EditorActionSpec.enter();
        }
    }
}
