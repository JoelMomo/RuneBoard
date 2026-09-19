package io.github.joelmomo.runeboard.suggestion;

import io.github.joelmomo.runeboard.editor.EditorInputPolicy;

public final class SuggestionPolicy {

    private SuggestionPolicy() {
    }

    public static boolean supportsInputType(int inputType) {
        return EditorInputPolicy.supportsSuggestions(inputType);
    }
}
