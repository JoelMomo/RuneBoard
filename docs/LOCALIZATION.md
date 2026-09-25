# RuneBoard localization

RuneBoard uses Android string resources as the localization boundary.

## Source locale

The source UI language is English and lives in:

`app/src/main/res/values/strings.xml`

Future translations should override those keys in locale-specific resource folders, for example:

- `values-es/strings.xml`
- `values-fr/strings.xml`
- `values-de/strings.xml`
- `values-it/strings.xml`

No Java/Kotlin UI rewrite should be required when adding one of those locales.

## Translation rules

Translate:

- settings section titles and descriptions;
- setup guidance and status labels;
- typing-assistance and feedback labels;
- Appearance labels, picker descriptions and color names;
- keyboard command labels such as Enter, Search, Send and Restore;
- controller-action descriptions;
- accessibility/service descriptions.

Keep fixed:

- the RuneBoard brand name and `R` mark;
- typeface names such as Inter, JetBrains Mono, Space Grotesk and MedievalSharp;
- physical controller legends such as A, B, X, Y, L1, R1, Start and Select;
- symbolic picker marks and checkmarks;
- keyboard-profile autonyms (English, Español, Français, Русский) and standard layout names such as QWERTY/AZERTY.

## Formatting

User-visible compositions must use formatted string resources instead of Java string concatenation.

Examples already covered:

- Appearance summary/value rows;
- percentages;
- keyboard-profile accessibility descriptions;
- keyboard-profile short-label/layout summaries.

This keeps word order, punctuation and RTL handling overridable per locale.

## Adding a locale

1. Copy only the translatable keys needed into a new `values-<locale>/strings.xml`.
2. Preserve placeholder indices such as `%1$s` and `%1$d`.
3. Run unit tests, Android lint and a settings-screen smoke test.
4. Check the settings screen for clipping at the Thor upper-display size.
5. Check command labels on the lower keyboard for width-sensitive translations.

Per-app language selection can be added when RuneBoard ships its first translated resource set; until then Android follows the system/app locale normally.
