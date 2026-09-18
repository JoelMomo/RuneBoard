package io.github.joelmomo.runeboard.suggestion;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class WordContextTest {

    @Test
    public void extractsTrailingWordAfterWhitespace() {
        WordContext context = WordContext.trailing("hello brave worl");
        assertEquals("worl", context.word);
        assertEquals(4, context.length);
    }

    @Test
    public void keepsApostrophesInsideWords() {
        assertEquals("don't", WordContext.trailing("say don't").word);
        assertEquals("l'amour", WordContext.trailing("de l'amour").word);
    }

    @Test
    public void supportsUnicodeLetters() {
        assertEquals("\u00f1and\u00fa",
                WordContext.trailing("hola \u00f1and\u00fa").word);
        assertEquals("\u043f\u0440\u0438\u0432\u0435\u0442",
                WordContext.trailing("x \u043f\u0440\u0438\u0432\u0435\u0442").word);
    }

    @Test
    public void punctuationEndsTheCurrentWord() {
        assertEquals("", WordContext.trailing("hello.").word);
        assertEquals("word", WordContext.trailing("hello,word").word);
    }
}

