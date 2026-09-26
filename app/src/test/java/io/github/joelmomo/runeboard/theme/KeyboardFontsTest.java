package io.github.joelmomo.runeboard.theme;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class KeyboardFontsTest {

  @Test
  public void bundledFontIdsNormalizeToThemselves() {
    assertEquals(KeyboardFonts.ID_SYSTEM, KeyboardFonts.normalizeId(KeyboardFonts.ID_SYSTEM));
    assertEquals(KeyboardFonts.ID_INTER, KeyboardFonts.normalizeId(KeyboardFonts.ID_INTER));
    assertEquals(KeyboardFonts.ID_ATKINSON, KeyboardFonts.normalizeId(KeyboardFonts.ID_ATKINSON));
    assertEquals(
        KeyboardFonts.ID_JETBRAINS_MONO,
        KeyboardFonts.normalizeId(KeyboardFonts.ID_JETBRAINS_MONO));
    assertEquals(
        KeyboardFonts.ID_SPACE_GROTESK, KeyboardFonts.normalizeId(KeyboardFonts.ID_SPACE_GROTESK));
    assertEquals(
        KeyboardFonts.ID_MEDIEVAL_SHARP,
        KeyboardFonts.normalizeId(KeyboardFonts.ID_MEDIEVAL_SHARP));
  }

  @Test
  public void unknownFontFallsBackToSystem() {
    assertEquals(KeyboardFonts.ID_SYSTEM, KeyboardFonts.normalizeId("missing"));
    assertEquals(KeyboardFonts.ID_SYSTEM, KeyboardFonts.normalizeId(null));
  }
}
