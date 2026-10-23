package dev.harvestrules;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

/**
 * Harvest Rules: right-click a mature crop to harvest and replant it. Server side, no dependencies, no mixins,
 * no ticking, no blocks/items/packets. Clients only need it for the config screen.
 */
@Mod(HarvestRules.MODID)
public final class HarvestRules {
    public static final String MODID = "harvest_rules";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Block> HARVESTABLE = TagKey.create(Registries.BLOCK, id("harvestable"));
    public static final TagKey<Block> EXCLUDED = TagKey.create(Registries.BLOCK, id("excluded"));

    public HarvestRules(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, HarvestConfig.SPEC);
        modBus.addListener(HarvestDataMaps::register);
        // LOW and not cancelled: claim/protection mods that cancel the click get their say first.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, PlayerInteractEvent.RightClickBlock.class,
                HarvestHandler::onRightClickBlock);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
