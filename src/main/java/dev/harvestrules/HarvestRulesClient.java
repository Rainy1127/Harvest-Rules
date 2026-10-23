package dev.harvestrules;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client-only entry point: just the config screen (Mods -> Harvest Rules -> Config). */
@Mod(value = HarvestRules.MODID, dist = Dist.CLIENT)
public final class HarvestRulesClient {
    public HarvestRulesClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
