# Suggestions and autocorrection

RuneBoard 0.0.10 adds typing assistance through Android text services without bundling a prediction engine or proprietary dictionary.

## Source

RuneBoard opens a SpellCheckerSession for the active typing profile locale.

Current supported typing locales:

- en-US
- es-ES
- fr-FR
- ru-RU

The session uses Android API 31+ SpellCheckerSessionParams and requests these result attributes:

- in dictionary
- looks like typo
- recommended suggestions

RuneBoard itself declares no INTERNET permission. Words are passed to the spell-check service selected by Android. The implementation and privacy behavior of that service belong to its provider, so RuneBoard does not claim that every system spell checker is offline.

## Suggestion bar

For the current word before the cursor:

- words shorter than two characters are ignored;
- password, visible-password, web-password, email, URI and TYPE_TEXT_FLAG_NO_SUGGESTIONS editors are ignored;
- up to three unique candidates are displayed in the existing header;
- the first candidate shows the mapped Accept suggestion controller button;
- candidates can be selected by touch;
- R3 accepts the first candidate by default.

Candidate rendering does not increase keyboard height. When no candidates exist the header returns to the normal D-PAD / background status.

## Stale-result protection

RuneBoard protects the editor from asynchronous stale results in two layers:

1. AndroidSpellSuggestionSource tracks request sequence IDs.
2. RuneBoardImeService verifies that the current word still matches the returned word before displaying or applying candidates.

Changing language closes the previous session generation so old-locale callbacks are ignored.

## Autocorrect

Autocorrect runs only when Space is pressed and every condition is true:

- Autocorrect is enabled;
- the result still matches the word at the cursor;
- Android marks the word as a typo;
- Android marks the suggestions as recommended;
- the word is not reported as already in the dictionary;
- a primary candidate exists and differs from the typed word.

This is intentionally conservative. Weak suggestions are displayed but are not applied automatically.

Case is preserved for normal title-case and all-uppercase input.

## Editor safety

RuneBoard only requests or applies spelling replacements when Android reports a collapsed text selection. If the editor has selected text, or if the cursor/selection state cannot be confirmed, RuneBoard does not replace a word. Space remains a normal space in that case.

Suggestion policy also rejects password, visible-password, web-password, email, URI, NO_SUGGESTIONS and non-text editors. These cases are covered by unit tests.

## Settings

Suggestions and Autocorrect are both enabled by default.

- turning Suggestions off also turns Autocorrect off;
- turning Autocorrect on turns Suggestions on;
- turning Suggestions off closes the current spell-check session;
- settings are persisted through RunePreferences.

## Emulator validation

Validated on the dedicated Android emulator using the system-selected Android spell checker.

Observed English examples:

- wrold -> world / wold / Harold, marked typo + recommended;
- speling -> spelling / spieling / spiking, marked typo + recommended;
- recieve -> receive / received / receiver, marked typo + recommended;
- helo and teh returned no correction from this provider.

Both acceptance paths were tested end to end:

- R3 changed recieve to receive;
- touching the first candidate changed wrold to world.

After either replacement the next spell-check request recognized the corrected word as valid/no longer suggested a replacement.

Autocorrect was also validated end to end: typing `recieve`, pressing the mapped Space button, then typing `test` produced `receive test` in the real EditText.

Settings dependency was validated through the UI: turning Suggestions off persisted both Suggestions=false and Autocorrect=false; enabling Autocorrect again restored both to true.
