package dev.harvestrules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * Block data map {@code harvest_rules:replant}
 * ({@code data/harvest_rules/data_maps/block/replant.json}), for crops that do not follow vanilla's pattern:
 * <pre>{ "values": { "somemod:pepper_crop": { "seed": "somemod:pepper_seeds", "reset_state": { "age": "0" }, "age_property": "age" } } }</pre>
 * All fields are optional. Not synced: only the server reads it.
 */
public final class HarvestDataMaps {
    /**
     * @param seed         the item taken from the drops to pay for replanting (default: the block's own item,
     *                     which is the seed for vanilla crops: wheat seeds, carrot, potato, beetroot seeds, nether wart,
     *                     cocoa beans)
     * @param resetState   block state properties after replanting, by name (default: the age property at its minimum)
     * @param ageProperty  name of the integer property that measures growth (default "age")
     */
    public record ReplantRule(Optional<Item> seed, Map<String, String> resetState, String ageProperty) {
        public static final Codec<ReplantRule> CODEC = RecordCodecBuilder.create(in -> in.group(
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("seed").forGetter(ReplantRule::seed),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("reset_state", Map.of()).forGetter(ReplantRule::resetState),
                Codec.STRING.optionalFieldOf("age_property", "age").forGetter(ReplantRule::ageProperty)
        ).apply(in, ReplantRule::new));

        public static final ReplantRule DEFAULT = new ReplantRule(Optional.empty(), Map.of(), "age");
    }

    public static final DataMapType<Block, ReplantRule> REPLANT =
            DataMapType.builder(HarvestRules.id("replant"), Registries.BLOCK, ReplantRule.CODEC).build();

    private HarvestDataMaps() {
    }

    static void register(RegisterDataMapTypesEvent event) {
        event.register(REPLANT);
    }
}
