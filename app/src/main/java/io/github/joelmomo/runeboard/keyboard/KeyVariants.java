package io.github.joelmomo.runeboard.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class KeyVariants {

  private KeyVariants() {}

  public static List<String> forKey(String text, Locale locale, boolean shifted) {
    if (text == null || text.codePointCount(0, text.length()) != 1) {
      return Collections.emptyList();
    }

    String lower = text.toLowerCase(Locale.ROOT);
    String language = locale == null ? "" : locale.getLanguage();
    List<String> values = variants(lower, language);
    if (values.isEmpty() || !shifted) {
      return values;
    }

    List<String> upper = new ArrayList<>(values.size());
    Locale casingLocale = locale == null ? Locale.ROOT : locale;
    for (String value : values) {
      upper.add(value.toUpperCase(casingLocale));
    }
    return Collections.unmodifiableList(upper);
  }

  private static List<String> variants(String key, String language) {
    switch (key) {
      case "a":
        return ordered(language, "á", "à", "ä", "â", "ã", "å", "æ");
      case "c":
        return ordered(language, "ç", "ć", "č");
      case "d":
        return List.of("ð", "ď");
      case "e":
        return ordered(language, "é", "è", "ê", "ë");
      case "е":
        return List.of("ё");
      case "i":
        return ordered(language, "í", "ì", "î", "ï");
      case "l":
        return List.of("ł");
      case "n":
        return ordered(language, "ñ", "ń", "ň");
      case "o":
        return ordered(language, "ó", "ò", "ö", "ô", "õ", "ø", "œ");
      case "s":
        return ordered(language, "ß", "ś", "š");
      case "u":
        if ("es".equals(language)) {
          return List.of("ú", "ü", "ù", "û");
        }
        return ordered(language, "ú", "ù", "ü", "û");
      case "y":
        return List.of("ý", "ÿ");
      case "z":
        return List.of("ž", "ź", "ż");
      default:
        return Collections.emptyList();
    }
  }

  private static List<String> ordered(String language, String... defaults) {
    if ("de".equals(language)) {
      if ("a".equals(baseFor(defaults))) {
        return List.of("ä", "á", "à", "â", "ã", "å", "æ");
      }
      if ("o".equals(baseFor(defaults))) {
        return List.of("ö", "ó", "ò", "ô", "õ", "ø", "œ");
      }
    }
    if ("fr".equals(language) && defaults.length > 0) {
      if ("ç".equals(defaults[0])) {
        return List.of(defaults);
      }
      if ("é".equals(defaults[0])) {
        return List.of("é", "è", "ê", "ë");
      }
    }
    return List.of(defaults);
  }

  private static String baseFor(String[] defaults) {
    if (defaults.length == 0) {
      return "";
    }
    if ("á".equals(defaults[0])) {
      return "a";
    }
    if ("ó".equals(defaults[0])) {
      return "o";
    }
    return "";
  }
}
