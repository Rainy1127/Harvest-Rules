package dev.harvestrules.logic;

import java.util.Collection;

/** The cheap yes/no questions asked before anything touches the world. Pure Java. */
public final class HarvestGate {
    private HarvestGate() {
    }

    /** Main hand only, server only; sneaking with something in the hand keeps vanilla's "use item on block". */
    public static boolean interactionQualifies(boolean mainHand, boolean clientSide, boolean sneaking, boolean handEmpty) {
        return mainHand && !clientSide && !(sneaking && !handEmpty);
    }

    /**
     * @param inHarvestableTag block is in {@code #harvest_rules:harvestable}
     * @param inExcludedTag    block is in {@code #harvest_rules:excluded} (always wins)
     * @param isCropBlock      block extends vanilla's CropBlock
     * @param autoDetect       config {@code auto_detect_crop_blocks}
     * @param hasVanillaHarvest block already has a right-click harvest in vanilla (sweet berries): never touched
     */
    public static boolean blockQualifies(boolean inHarvestableTag, boolean inExcludedTag, boolean isCropBlock,
                                         boolean autoDetect, boolean hasVanillaHarvest) {
        if (hasVanillaHarvest || inExcludedTag) {
            return false;
        }
        return inHarvestableTag || (autoDetect && isCropBlock);
    }

    /** Mature = the age is the largest value the property allows. Empty or unknown ranges are never mature. */
    public static boolean isMature(int age, Collection<Integer> possibleAges) {
        if (possibleAges == null || possibleAges.isEmpty()) {
            return false;
        }
        int max = Integer.MIN_VALUE;
        for (int value : possibleAges) {
            max = Math.max(max, value);
        }
        return age >= max;
    }

    /** The age a replanted crop starts at: the smallest allowed value (0 for every vanilla crop). */
    public static int youngestAge(Collection<Integer> possibleAges) {
        int min = Integer.MAX_VALUE;
        for (int value : possibleAges) {
            min = Math.min(min, value);
        }
        return possibleAges.isEmpty() ? 0 : min;
    }
}
