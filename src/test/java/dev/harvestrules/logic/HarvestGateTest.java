package dev.harvestrules.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class HarvestGateTest {
    @Test
    void onlyMainHandOnServer() {
        assertTrue(HarvestGate.interactionQualifies(true, false, false, true));
        assertFalse(HarvestGate.interactionQualifies(false, false, false, true));
        assertFalse(HarvestGate.interactionQualifies(true, true, false, true));
    }

    @Test
    void sneakingWithItemKeepsVanillaUse() {
        assertFalse(HarvestGate.interactionQualifies(true, false, true, false));
        assertTrue(HarvestGate.interactionQualifies(true, false, true, true));
        assertTrue(HarvestGate.interactionQualifies(true, false, false, false));
    }

    @Test
    void blockRules() {
        assertTrue(HarvestGate.blockQualifies(true, false, false, false, false));
        assertTrue(HarvestGate.blockQualifies(false, false, true, true, false));
        assertFalse(HarvestGate.blockQualifies(false, false, true, false, false));
        assertFalse(HarvestGate.blockQualifies(true, true, true, true, false), "excluded wins");
        assertFalse(HarvestGate.blockQualifies(true, false, true, true, true), "sweet berries are never touched");
        assertFalse(HarvestGate.blockQualifies(false, false, false, true, false));
    }

    @Test
    void maturity() {
        List<Integer> wheat = List.of(0, 1, 2, 3, 4, 5, 6, 7);
        assertTrue(HarvestGate.isMature(7, wheat));
        assertFalse(HarvestGate.isMature(6, wheat));
        assertTrue(HarvestGate.isMature(2, List.of(0, 1, 2)));
        assertFalse(HarvestGate.isMature(3, List.of()));
        assertFalse(HarvestGate.isMature(3, null));
    }

    @Test
    void youngestAge() {
        assertEquals(0, HarvestGate.youngestAge(List.of(3, 0, 7)));
        assertEquals(2, HarvestGate.youngestAge(List.of(2, 3)));
        assertEquals(0, HarvestGate.youngestAge(List.of()));
    }

    @Test
    void settingsAreClamped() {
        HarvestSettings s = new HarvestSettings(true, false, 500, Float.NaN, -3, true);
        assertEquals(HarvestSettings.MAX_DURABILITY_COST, s.hoeDurabilityCost());
        assertEquals(0f, s.exhaustion(), 0f);
        assertEquals(0, s.xpPerHarvest());
        assertEquals(HarvestSettings.MAX_EXHAUSTION, new HarvestSettings(true, false, 1, 99f, 0, true).exhaustion(), 0f);
    }
}
