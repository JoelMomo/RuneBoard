package io.github.joelmomo.runeboard.language;

import io.github.joelmomo.runeboard.keyboard.KeyboardLayouts;

import java.util.List;

public final class KeyboardProfiles {

    public static final String ID_EN_US = "en-US";
    public static final String ID_ES_ES = "es-ES";
    public static final String ID_FR_FR = "fr-FR";
    public static final String ID_RU_RU = "ru-RU";

    private static final List<KeyboardProfile> BUILT_INS = List.of(
            new KeyboardProfile(
                    ID_EN_US,
                    "en-US",
                    "English",
                    "QWERTY",
                    "EN",
                    KeyboardLayouts.englishQwerty(),
                    KeyboardLayouts.englishSymbols()),
            new KeyboardProfile(
                    ID_ES_ES,
                    "es-ES",
                    "Espa\u00f1ol",
                    "QWERTY",
                    "ES",
                    KeyboardLayouts.spanishQwerty(),
                    KeyboardLayouts.spanishSymbols()),
            new KeyboardProfile(
                    ID_FR_FR,
                    "fr-FR",
                    "Fran\u00e7ais",
                    "AZERTY",
                    "FR",
                    KeyboardLayouts.frenchAzerty(),
                    KeyboardLayouts.frenchSymbols()),
            new KeyboardProfile(
                    ID_RU_RU,
                    "ru-RU",
                    "\u0420\u0443\u0441\u0441\u043a\u0438\u0439",
                    "\u0419\u0426\u0423\u041a\u0415\u041d",
                    "RU",
                    KeyboardLayouts.russianJcuken(),
                    KeyboardLayouts.russianSymbols()));

    private KeyboardProfiles() {
    }

    public static KeyboardProfile defaultProfile() {
        return BUILT_INS.get(0);
    }

    public static KeyboardProfile byId(String id) {
        if (id != null) {
            for (KeyboardProfile profile : BUILT_INS) {
                if (profile.id.equals(id)) {
                    return profile;
                }
            }
        }
        return defaultProfile();
    }

    public static KeyboardProfile next(String currentId) {
        KeyboardProfile current = byId(currentId);
        int index = BUILT_INS.indexOf(current);
        return BUILT_INS.get((index + 1) % BUILT_INS.size());
    }

    public static List<KeyboardProfile> builtIns() {
        return BUILT_INS;
    }
}
