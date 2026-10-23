package dev.harvestrules.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeedLogicTest {
    /** Stand-in for ItemStack. */
    static final class Stack {
        final String item;
        int count;

        Stack(String item, int count) {
            this.item = item;
            this.count = count;
        }
    }

    private static int find(List<Stack> drops, String seed) {
        return SeedLogic.findSeed(drops, s -> s.item.equals(seed), s -> s.count);
    }

    @Test
    void wheatPaysWithOneSeed() {
        List<Stack> drops = new ArrayList<>(List.of(new Stack("wheat", 1), new Stack("wheat_seeds", 2)));
        int index = find(drops, "wheat_seeds");
        assertEquals(1, index);
        drops.get(index).count--;
        assertEquals(1, drops.get(1).count);
        assertEquals(1, drops.get(0).count, "the harvest itself is untouched");
    }

    @Test
    void carrotsUseTheCropAsSeed() {
        List<Stack> drops = List.of(new Stack("carrot", 3));
        assertEquals(0, find(drops, "carrot"));
    }

    @Test
    void emptyStacksAreSkipped() {
        List<Stack> drops = List.of(new Stack("wheat_seeds", 0), new Stack("wheat", 1), new Stack("wheat_seeds", 1));
        assertEquals(2, find(drops, "wheat_seeds"));
    }

    @Test
    void noSeedInDrops() {
        assertEquals(-1, find(List.of(new Stack("wheat", 1)), "wheat_seeds"));
        assertEquals(-1, find(List.of(), "wheat_seeds"));
    }

    @Test
    void sourceOrder() {
        assertEquals(SeedLogic.Source.DROPS, SeedLogic.chooseSource(true, true, true, false));
        assertEquals(SeedLogic.Source.INVENTORY, SeedLogic.chooseSource(true, false, true, false));
        assertEquals(SeedLogic.Source.NONE, SeedLogic.chooseSource(true, false, false, false));
        assertEquals(SeedLogic.Source.FREE, SeedLogic.chooseSource(true, false, false, true));
        assertEquals(SeedLogic.Source.DROPS, SeedLogic.chooseSource(true, true, false, true));
        assertEquals(SeedLogic.Source.NONE, SeedLogic.chooseSource(false, true, true, false), "unknown seed: never free in survival");
        assertEquals(SeedLogic.Source.FREE, SeedLogic.chooseSource(false, false, false, true));
    }
}
