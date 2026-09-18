package io.github.joelmomo.runeboard.suggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SuggestionResult {

    public final String word;
    public final List<String> candidates;
    public final boolean inDictionary;
    public final boolean looksLikeTypo;
    public final boolean recommended;

    public SuggestionResult(
            String word,
            List<String> candidates,
            boolean inDictionary,
            boolean looksLikeTypo,
            boolean recommended) {
        this.word = word == null ? "" : word;
        this.candidates = Collections.unmodifiableList(
                new ArrayList<>(candidates));
        this.inDictionary = inDictionary;
        this.looksLikeTypo = looksLikeTypo;
        this.recommended = recommended;
    }

    public static SuggestionResult empty(String word) {
        return new SuggestionResult(
                word,
                Collections.emptyList(),
                false,
                false,
                false);
    }

    public String primary() {
        return candidates.isEmpty() ? null : candidates.get(0);
    }
}
