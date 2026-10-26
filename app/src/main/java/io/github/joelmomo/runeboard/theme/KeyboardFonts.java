package io.github.joelmomo.runeboard.theme;

import android.content.Context;
import android.graphics.Typeface;
import io.github.joelmomo.runeboard.R;
import java.io.File;

public final class KeyboardFonts {

  public static final String ID_SYSTEM = "system";
  public static final String ID_INTER = "inter";
  public static final String ID_ATKINSON = "atkinson";
  public static final String ID_JETBRAINS_MONO = "jetbrains_mono";
  public static final String ID_SPACE_GROTESK = "space_grotesk";
  public static final String ID_MEDIEVAL_SHARP = "medieval_sharp";
  public static final String ID_NOTHING_NDOT = "nothing_ndot";

  /**
   * Nothing's dot-matrix face ships with Nothing OS. It is read from the device at runtime and
   * never bundled, so it is only offered where the system provides it.
   */
  static final String NOTHING_NDOT_PATH = "/system/fonts/Ndot-57.otf";

  private KeyboardFonts() {}

  public static String normalizeId(String id) {
    if (ID_INTER.equals(id)
        || ID_ATKINSON.equals(id)
        || ID_JETBRAINS_MONO.equals(id)
        || ID_SPACE_GROTESK.equals(id)
        || ID_MEDIEVAL_SHARP.equals(id)
        || ID_NOTHING_NDOT.equals(id)) {
      return id;
    }
    return ID_SYSTEM;
  }

  public static Typeface resolve(Context context, String id) {
    switch (normalizeId(id)) {
      case ID_INTER:
        return context.getResources().getFont(R.font.inter);
      case ID_ATKINSON:
        return context.getResources().getFont(R.font.atkinson_hyperlegible);
      case ID_JETBRAINS_MONO:
        return context.getResources().getFont(R.font.jetbrains_mono);
      case ID_SPACE_GROTESK:
        return context.getResources().getFont(R.font.space_grotesk);
      case ID_MEDIEVAL_SHARP:
        return context.getResources().getFont(R.font.medieval_sharp);
      case ID_NOTHING_NDOT:
        return systemFont(NOTHING_NDOT_PATH);
      default:
        return Typeface.DEFAULT;
    }
  }

  public static boolean isNothingNdotAvailable() {
    return new File(NOTHING_NDOT_PATH).canRead();
  }

  private static Typeface systemFont(String path) {
    File file = new File(path);
    if (!file.canRead()) {
      return Typeface.DEFAULT;
    }
    try {
      return Typeface.createFromFile(file);
    } catch (RuntimeException unreadable) {
      return Typeface.DEFAULT;
    }
  }
}
