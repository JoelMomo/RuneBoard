package io.github.joelmomo.runeboard.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public final class KeyboardProfilesTest {

    @Test
    public void catalogContainsFourInitialTypingProfiles() {
        assertEquals(4, KeyboardProfiles.builtIns().size());
        assertEquals(
                KeyboardProfiles.ID_EN_US,
                KeyboardProfiles.builtIns().get(0).id);
        assertEquals(
                KeyboardProfiles.ID_ES_ES,
                KeyboardProfiles.builtIns().get(1).id);
        assertEquals(
                KeyboardProfiles.ID_FR_FR,
                KeyboardProfiles.builtIns().get(2).id);
        assertEquals(
                KeyboardProfiles.ID_RU_RU,
                KeyboardProfiles.builtIns().get(3).id);
    }

    @Test
    public void nextCyclesThroughAllProfiles() {
        assertEquals(KeyboardProfiles.ID_ES_ES,
                KeyboardProfiles.next(KeyboardProfiles.ID_EN_US).id);
        assertEquals(KeyboardProfiles.ID_FR_FR,
                KeyboardProfiles.next(KeyboardProfiles.ID_ES_ES).id);
        assertEquals(KeyboardProfiles.ID_RU_RU,
                KeyboardProfiles.next(KeyboardProfiles.ID_FR_FR).id);
        assertEquals(KeyboardProfiles.ID_EN_US,
                KeyboardProfiles.next(KeyboardProfiles.ID_RU_RU).id);
    }

    @Test
    public void unknownProfileFallsBackToEnglish() {
        assertSame(
                KeyboardProfiles.defaultProfile(),
                KeyboardProfiles.byId("unknown"));
    }
}
