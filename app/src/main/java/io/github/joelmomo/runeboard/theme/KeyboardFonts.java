package io.github.joelmomo.runeboard.theme;

import android.content.Context;
import android.graphics.Typeface;
import io.github.joelmomo.runeboard.R;

public final class KeyboardFonts {

  public static final String ID_SYSTEM = "system";
  public static final String ID_INTER = "inter";
  public static final String ID_ATKINSON = "atkinson";
  public static final String ID_JETBRAINS_MONO = "jetbrains_mono";
  public static final String ID_SPACE_GROTESK = "space_grotesk";

  private KeyboardFonts() {}

  public static String normalizeId(String id) {
    if (ID_INTER.equals(id)
        || ID_ATKINSON.equals(id)
        || ID_JETBRAINS_MONO.equals(id)
        || ID_SPACE_GROTESK.equals(id)) {
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
      default:
        return Typeface.DEFAULT;
    }
  }
}
