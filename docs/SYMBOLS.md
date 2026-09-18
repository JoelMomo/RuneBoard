# Symbols, punctuation and accents

RuneBoard 0.0.11 adds a second keyboard page for punctuation and language-specific characters.

## Alphabet page

The utility row now contains:

- Shift
- SYM
- comma
- Space
- period
- Backspace
- Enter
- background opacity
- Minimize

Comma and period therefore remain directly available without leaving the alphabet layout.

## Symbol page

Press SYM to switch to the symbol page. The same key becomes ABC and returns to the alphabet page.

The page keeps:

- the always-visible number row;
- common punctuation and operators;
- brackets and slashes;
- Space, Backspace, Enter, opacity and Minimize;
- D-pad and touch navigation.

The controller focus stays on the MODE key when changing pages so A can immediately toggle SYM / ABC again.

## Language-specific extras

English includes common ASCII symbols plus pound and euro signs.

Spanish includes accented vowels, diaeresis, enye, inverted question/exclamation marks and euro.

French includes common accented vowels and cedilla.

Russian includes yo / capital yo plus common Russian-oriented punctuation and currency symbols.

## IME geometry

Switching ABC / SYM changes key geometry but not keyboard height.

RuneBoard therefore uses a dedicated KeyboardEngine.Update.GEOMETRY result. The view rebuilds hit targets and redraws without calling requestLayout().

This is intentionally separate from Update.LAYOUT, which is reserved for real height changes such as minimize / restore.

The distinction fixes an Android emulator issue where requesting a full IME layout pass during SYM switching could hide the input view.

## Navigation-bar inset

RuneKeyboardView subtracts the bottom navigation-bar inset when calculating key-row geometry.

This keeps the utility row above Android system navigation and improves touchability on the 1240x1080 regression emulator.

## Validation

JVM tests cover:

- ABC / SYM state switching;
- focus retention on the MODE key;
- D-pad navigation through symbol mode;
- direct comma and period keys;
- Spanish, French and Russian symbol extras.

End-to-end emulator validation used the real RuneBoard IME and controller path:

1. navigate from the initial alphabet key to SYM with D-pad;
2. press A;
3. confirm the IME remains visible;
4. navigate to the first symbol and type it;
5. return to ABC;
6. type a normal alphabet character.

The resulting EditText content was `!e`.

Physical AYN Thor validation for this specific 0.0.11 feature is still pending.
