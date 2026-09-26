package io.github.joelmomo.runeboard.keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import org.junit.Test;

public final class KeyVariantsTest {

  @Test
  public void spanishVariantsPrioritizeExpectedAccents() {
    Locale spanish = Locale.forLanguageTag("es-ES");

    assertEquals("á", KeyVariants.forKey("a", spanish, false).get(0));
    assertEquals(List.of("ú", "ü", "ù", "û"), KeyVariants.forKey("u", spanish, false));
    assertEquals("ñ", KeyVariants.forKey("n", spanish, false).get(0));
  }

  @Test
  public void shiftedVariantsAreUppercase() {
    List<String> variants = KeyVariants.forKey("a", Locale.forLanguageTag("es-ES"), true);

    assertEquals("Á", variants.get(0));
    assertTrue(variants.contains("Ä"));
  }

  @Test
  public void russianEOffersYo() {
    assertEquals(List.of("ё"), KeyVariants.forKey("е", Locale.forLanguageTag("ru-RU"), false));
    assertEquals(List.of("Ё"), KeyVariants.forKey("е", Locale.forLanguageTag("ru-RU"), true));
  }

  @Test
  public void unsupportedKeysHaveNoVariants() {
    assertTrue(KeyVariants.forKey("q", Locale.ENGLISH, false).isEmpty());
    assertTrue(KeyVariants.forKey(".", Locale.ENGLISH, false).isEmpty());
  }
}
