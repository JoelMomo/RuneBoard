package io.github.joelmomo.runeboard.suggestion;

public interface SuggestionSource extends AutoCloseable {

    interface Callback {
        void onSuggestions(SuggestionResult result);
    }

    boolean isAvailable();

    void request(String word, Callback callback);

    @Override
    void close();
}
