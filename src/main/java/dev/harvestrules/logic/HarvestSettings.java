package dev.harvestrules.logic;

/** Config values, already clamped. */
public record HarvestSettings(boolean autoDetectCropBlocks, boolean requireHoe, int hoeDurabilityCost, float exhaustion,
                              int xpPerHarvest, boolean respectProtection) {
    public static final int MAX_DURABILITY_COST = 100;
    public static final float MAX_EXHAUSTION = 10f;
    public static final int MAX_XP = 100;

    public HarvestSettings {
        hoeDurabilityCost = Math.max(0, Math.min(MAX_DURABILITY_COST, hoeDurabilityCost));
        exhaustion = Float.isNaN(exhaustion) ? 0f : Math.max(0f, Math.min(MAX_EXHAUSTION, exhaustion));
        xpPerHarvest = Math.max(0, Math.min(MAX_XP, xpPerHarvest));
    }
}
