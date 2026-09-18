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
                    KeyboardLayouts.englishQwerty()),
            new KeyboardProfile(
                    ID_ES_ES,
                    "es-ES",
                    "EspaÃ±ol",
                    "QWERTY",
                    "ES",
                    KeyboardLayouts.spanishQwerty()),
            new KeyboardProfile(
                    ID_FR_FR,
                    "fr-FR",
                    "FranÃ§ais",
                    "AZERTY",
                    "FR",
                    KeyboardLayouts.frenchAzerty()),
            new KeyboardProfile(
                    ID_RU_RU,
                    "ru-RU",
                    "Ð ÑƒÑÑÐºÐ¸Ð¹",
                    "Ð™Ð¦Ð£ÐšÐ•Ð",
                    "RU",
                    KeyboardLayouts.russianJcuken()));

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
