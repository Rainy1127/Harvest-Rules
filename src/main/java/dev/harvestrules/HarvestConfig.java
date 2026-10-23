package dev.harvestrules;

import dev.harvestrules.logic.HarvestSettings;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config: {@code <world>/serverconfig/harvest_rules-server.toml}. Read on every harvest, so edits apply live. */
public final class HarvestConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue AUTO_DETECT_CROP_BLOCKS = BUILDER
            .comment("Also harvest every block that extends vanilla's CropBlock (most modded crops), not only the tag.",
                    "#harvest_rules:excluded still wins.")
            .translation("harvest_rules.configuration.auto_detect_crop_blocks")
            .define("auto_detect_crop_blocks", true);

    public static final ModConfigSpec.BooleanValue REQUIRE_HOE = BUILDER
            .comment("Only harvest when the player holds a hoe (#minecraft:hoes) in the main hand.")
            .translation("harvest_rules.configuration.require_hoe")
            .define("require_hoe", false);

    public static final ModConfigSpec.IntValue HOE_DURABILITY_COST = BUILDER
            .comment("Durability a held hoe loses per harvest (0 = none). Hoe enchantments such as Unbreaking apply.")
            .translation("harvest_rules.configuration.hoe_durability_cost")
            .defineInRange("hoe_durability_cost", 1, 0, 100);

    public static final ModConfigSpec.DoubleValue EXHAUSTION = BUILDER
            .comment("Food exhaustion added per harvest (vanilla block breaking: 0.005).")
            .translation("harvest_rules.configuration.exhaustion")
            .defineInRange("exhaustion", 0.005, 0.0, 10.0);

    public static final ModConfigSpec.IntValue XP_PER_HARVEST = BUILDER
            .comment("Experience points dropped per harvest (0 = none).")
            .translation("harvest_rules.configuration.xp_per_harvest")
            .defineInRange("xp_per_harvest", 0, 0, 100);

    public static final ModConfigSpec.BooleanValue RESPECT_PROTECTION = BUILDER
            .comment("Treat a harvest like breaking the block for protection: adventure mode, spawn protection and",
                    "claim mods that listen to block breaking can stop it. Turn off only if a mod blocks harvesting by mistake.")
            .translation("harvest_rules.configuration.respect_protection")
            .define("respect_protection", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private HarvestConfig() {
    }

    public static HarvestSettings settings() {
        try {
            return new HarvestSettings(AUTO_DETECT_CROP_BLOCKS.get(), REQUIRE_HOE.get(), HOE_DURABILITY_COST.get(),
                    EXHAUSTION.get().floatValue(), XP_PER_HARVEST.get(), RESPECT_PROTECTION.get());
        } catch (RuntimeException notLoaded) {
            return new HarvestSettings(true, false, 1, 0.005f, 0, true);
        }
    }
}
