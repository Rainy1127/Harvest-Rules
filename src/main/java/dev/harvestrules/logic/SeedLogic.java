package dev.harvestrules.logic;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Where the seed for replanting comes from. Pure Java and generic, so it is tested with plain records.
 * <ol>
 *   <li>One seed is taken out of the harvest drops (the normal case: the crop "pays" for its own replanting).</li>
 *   <li>If the drops have none (wheat can roll 0 seeds), one seed is taken from the player's inventory.</li>
 *   <li>If there is none there either, the crop is harvested but not replanted (exactly like breaking it by hand).
 *       Players in creative mode always replant for free.</li>
 * </ol>
 * There is never a free replant in survival, so no crop can become an endless source.
 */
public final class SeedLogic {
    public enum Source { DROPS, INVENTORY, FREE, NONE }

    private SeedLogic() {
    }

    /** Index of the first stack that is the seed and not empty, or -1. */
    public static <S> int findSeed(List<S> stacks, Predicate<S> isSeed, ToIntFunction<S> count) {
        for (int i = 0; i < stacks.size(); i++) {
            S stack = stacks.get(i);
            if (count.applyAsInt(stack) > 0 && isSeed.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static Source chooseSource(boolean seedKnown, boolean inDrops, boolean inInventory, boolean creative) {
        if (seedKnown && inDrops) {
            return Source.DROPS;
        }
        if (creative) {
            return Source.FREE;
        }
        if (seedKnown && inInventory) {
            return Source.INVENTORY;
        }
        return Source.NONE;
    }
}
