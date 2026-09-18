package io.github.joelmomo.runeboard.suggestion;

import android.content.Context;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AndroidSpellSuggestionSource
        implements SuggestionSource,
        SpellCheckerSession.SpellCheckerSessionListener {

    private static final int COOKIE = 0x52554E45;
    private static final int LIMIT = 3;

    private SpellCheckerSession session;
    private int nextSequence = 1;
    private int latestSequence;
    private String latestWord = "";
    private Callback latestCallback;

    public AndroidSpellSuggestionSource(
            Context context,
            Locale locale) {
        TextServicesManager manager =
                context.getSystemService(TextServicesManager.class);
        if (manager == null) {
            return;
        }

        int attributes =
                SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY
                        | SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO
                        | SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS;

        try {
            SpellCheckerSession.SpellCheckerSessionParams params =
                    new SpellCheckerSession.SpellCheckerSessionParams.Builder()
                            .setLocale(locale)
                            .setShouldReferToSpellCheckerLanguageSettings(false)
                            .setSupportedAttributes(attributes)
                            .build();
            session = manager.newSpellCheckerSession(
                    params,
                    context.getMainExecutor(),
                    this);
        } catch (RuntimeException ignored) {
            session = null;
        }
    }

    @Override
    public boolean isAvailable() {
        return session != null && !session.isSessionDisconnected();
    }

    @Override
    public void request(String word, Callback callback) {
        latestCallback = callback;
        latestWord = word == null ? "" : word;
        latestSequence = nextSequence++;

        if (latestWord.isEmpty() || session == null) {
            callback.onSuggestions(SuggestionResult.empty(latestWord));
            return;
        }

        session.cancel();
        TextInfo info = new TextInfo(
                latestWord,
                COOKIE,
                latestSequence);
        session.getSentenceSuggestions(
                new TextInfo[] { info },
                LIMIT);
    }

    @Override
    public void onGetSentenceSuggestions(
            SentenceSuggestionsInfo[] results) {
        if (results == null || results.length == 0) {
            deliverEmpty();
            return;
        }

        SentenceSuggestionsInfo sentence = results[0];
        if (sentence == null || sentence.getSuggestionsCount() <= 0) {
            deliverEmpty();
            return;
        }

        SuggestionsInfo best = null;
        for (int index = 0;
                index < sentence.getSuggestionsCount();
                index++) {
            SuggestionsInfo info = sentence.getSuggestionsInfoAt(index);
            if (info != null && info.getSequence() == latestSequence) {
                best = info;
                break;
            }
        }

        if (best == null) {
            return;
        }

        deliver(best);
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        if (results == null) {
            deliverEmpty();
            return;
        }
        for (SuggestionsInfo info : results) {
            if (info != null && info.getSequence() == latestSequence) {
                deliver(info);
                return;
            }
        }
    }

    private void deliver(SuggestionsInfo info) {
        Callback callback = latestCallback;
        if (callback == null) {
            return;
        }

        int attributes = info.getSuggestionsAttributes();
        boolean inDictionary =
                (attributes & SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY) != 0;
        boolean typo =
                (attributes & SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO) != 0;
        boolean recommended =
                (attributes
                        & SuggestionsInfo.RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS) != 0;

        Set<String> unique = new LinkedHashSet<>();
        int count = info.getSuggestionsCount();
        for (int index = 0; index < count && unique.size() < LIMIT; index++) {
            String candidate = info.getSuggestionAt(index);
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            if (candidate.equalsIgnoreCase(latestWord)) {
                continue;
            }
            unique.add(candidate);
        }

        List<String> candidates = new ArrayList<>(unique);
        callback.onSuggestions(new SuggestionResult(
                latestWord,
                candidates,
                inDictionary,
                typo,
                recommended));
    }

    private void deliverEmpty() {
        Callback callback = latestCallback;
        if (callback != null) {
            callback.onSuggestions(SuggestionResult.empty(latestWord));
        }
    }

    @Override
    public void close() {
        if (session != null) {
            session.cancel();
            session.close();
            session = null;
        }
        latestCallback = null;
    }
}
