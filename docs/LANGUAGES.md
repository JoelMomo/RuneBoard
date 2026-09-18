# Typing languages and layouts

RuneBoard 0.0.9 separates typing language from the renderer and controller engine.

## Built-in profiles

| Profile | Locale | Layout |
| --- | --- | --- |
| English | en-US | QWERTY |
| Spanish | es-ES | QWERTY with N-tilde |
| French | fr-FR | AZERTY |
| Russian | ru-RU | JCUKEN |

All four profiles retain:

- always-visible number row;
- the same Thor utility row;
- D-pad navigation;
- touch input;
- controller remapping;
- Shift/Caps behavior;
- background/theme settings.

Uppercase conversion uses the profile locale instead of Locale.ROOT.

## Switching

A typing profile can be changed in three ways:

1. Settings -> Language & Layout.
2. The remappable Next language controller action, L3 by default.
3. Tap the language/layout area in the RuneBoard header.

The quick cycle is:

EN -> ES -> FR -> RU -> EN

The selected profile is persisted in RunePreferences and is loaded before the IME view is created.

## Controller behavior

Next language is a normal BindableAction.

- default key: L3;
- can be moved to another supported controller button;
- conflict handling uses the same automatic swap behavior as the other actions;
- it is not captured while RuneBoard is minimized, so L3 can continue to reach the game in compact mode.

## Japanese

Japanese is deliberately not represented as a simple static key-row profile.

A useful Japanese IME needs at least:

- kana composition;
- dakuten / handakuten handling;
- romaji-to-kana or direct kana input;
- candidate conversion;
- kana/kanji candidate selection.

RuneBoard will add Japanese only after the composition/suggestion subsystem can support it correctly.

## Validation

The four layouts and locale-aware casing are covered by JVM tests.

On the dedicated Android emulator:

- all four profile cards are visible;
- L3 cycles EN -> ES -> FR -> RU -> EN;
- each change persists immediately;
- tapping the header also cycles the active profile;
- the IME stays visible while the profile view is recreated.


## Symbols and accents

Each typing profile now owns both an alphabet layout and a language-specific symbol layout.

- English: common symbols plus pound/euro.
- Spanish: accented vowels, diaeresis, enye, inverted question/exclamation marks and euro.
- French: common accented vowels and cedilla.
- Russian: yo variants plus common Russian-oriented punctuation/currency symbols.

The utility-row SYM key switches to the symbol page and becomes ABC there. Controller focus stays on MODE across the transition so A can toggle back immediately.
