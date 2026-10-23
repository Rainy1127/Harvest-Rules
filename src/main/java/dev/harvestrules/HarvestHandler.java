package dev.harvestrules;

import dev.harvestrules.logic.HarvestGate;
import dev.harvestrules.logic.HarvestSettings;
import dev.harvestrules.logic.SeedLogic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Right-click harvest. Every check is ordered cheapest first; nothing runs unless a player right-clicks a block,
 * and almost every click leaves after a couple of field reads.
 */
public final class HarvestHandler {
    /** Vanilla level event id for "block destroyed": break particles and the block's break sound on every client. */
    private static final int DESTROY_BLOCK_EFFECT = 2001;

    private HarvestHandler() {
    }

    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!HarvestGate.interactionQualifies(event.getHand() == InteractionHand.MAIN_HAND, event.getLevel().isClientSide(),
                event.getEntity().isSecondaryUseActive(), event.getEntity().getMainHandItem().isEmpty())) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)
                || player.isSpectator()) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        HarvestSettings settings = HarvestConfig.settings();
        if (!HarvestGate.blockQualifies(state.is(HarvestRules.HARVESTABLE), state.is(HarvestRules.EXCLUDED),
                block instanceof CropBlock, settings.autoDetectCropBlocks(), block instanceof SweetBerryBushBlock)) {
            return;
        }

        HarvestDataMaps.ReplantRule rule = block.builtInRegistryHolder().getData(HarvestDataMaps.REPLANT);
        if (rule == null) {
            rule = HarvestDataMaps.ReplantRule.DEFAULT;
        }
        IntegerProperty age = findAge(state, rule.ageProperty());
        if (age == null || !isMature(state, block, age)) {
            return; // immature (or no growth property): the normal interaction goes on
        }

        ItemStack tool = player.getMainHandItem();
        boolean holdsHoe = tool.is(ItemTags.HOES);
        if (settings.requireHoe() && !holdsHoe) {
            return;
        }
        if (!player.mayInteract(level, pos)) {
            return;
        }
        if (settings.respectProtection()) {
            BreakBlockEvent check = CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, state);
            if (check.isCanceled()) {
                return;
            }
        }

        harvest(event, player, level, pos, state, rule, age, tool, holdsHoe, settings);
    }

    private static void harvest(PlayerInteractEvent.RightClickBlock event, ServerPlayer player, ServerLevel level, BlockPos pos,
                                BlockState state, HarvestDataMaps.ReplantRule rule, IntegerProperty age, ItemStack tool,
                                boolean holdsHoe, HarvestSettings settings) {
        Block block = state.getBlock();
        boolean creative = player.isCreative();

        // 1. Drops from the loot table, with the held tool (Fortune on a hoe works like breaking the crop).
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool));

        // 2. Pay for the replant with one seed.
        Item seed = rule.seed().orElse(block.asItem());
        boolean seedKnown = seed != Items.AIR;
        int inDrops = seedKnown ? SeedLogic.findSeed(drops, s -> s.is(seed), ItemStack::getCount) : -1;
        Inventory inventory = player.getInventory();
        int inInventory = -1;
        if (seedKnown && inDrops < 0 && !creative) {
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty() && stack.is(seed)) {
                    inInventory = slot;
                    break;
                }
            }
        }
        SeedLogic.Source source = SeedLogic.chooseSource(seedKnown, inDrops >= 0, inInventory >= 0, creative);
        switch (source) {
            case DROPS -> drops.get(inDrops).shrink(1);
            case INVENTORY -> {
                inventory.getItem(inInventory).shrink(1);
                inventory.setChanged();
            }
            case FREE, NONE -> {
            }
        }

        // 3. Replant (or clear the block when there was no seed anywhere), then spill the drops.
        BlockState replanted = source == SeedLogic.Source.NONE ? Blocks.AIR.defaultBlockState() : resetState(state, rule, age);
        level.setBlock(pos, replanted, Block.UPDATE_ALL);
        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                Block.popResource(level, pos, stack);
            }
        }

        // 4. Feedback: vanilla break particles + sound, game event for sculk sensors, arm swing.
        level.levelEvent(DESTROY_BLOCK_EFFECT, pos, Block.getId(state));
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        player.swing(InteractionHand.MAIN_HAND, true);

        // 5. Costs and rewards.
        if (!creative) {
            if (holdsHoe && settings.hoeDurabilityCost() > 0 && tool.isDamageableItem()) {
                tool.hurtAndBreak(settings.hoeDurabilityCost(), player, EquipmentSlot.MAINHAND);
            }
            if (settings.exhaustion() > 0f) {
                player.causeFoodExhaustion(settings.exhaustion());
            }
        }
        if (settings.xpPerHarvest() > 0) {
            ExperienceOrb.award(level, Vec3.atCenterOf(pos), settings.xpPerHarvest());
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    /** The growth property: by name from the data map ("age" by default). Null if the block has none. */
    private static IntegerProperty findAge(BlockState state, String name) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && property.getName().equals(name)) {
                return integer;
            }
        }
        return null;
    }

    private static boolean isMature(BlockState state, Block block, IntegerProperty age) {
        if (block instanceof CropBlock crop) {
            // CropBlock knows best (torchflower crops, for example, turn into another block at "max age").
            return crop.isMaxAge(state);
        }
        Collection<Integer> values = age.getPossibleValues();
        return HarvestGate.isMature(state.getValue(age), values);
    }

    /** Age back to its minimum (other properties such as the cocoa's facing stay), then the data map's overrides. */
    private static BlockState resetState(BlockState state, HarvestDataMaps.ReplantRule rule, IntegerProperty age) {
        BlockState result = state.setValue(age, HarvestGate.youngestAge(age.getPossibleValues()));
        for (Map.Entry<String, String> entry : rule.resetState().entrySet()) {
            for (Property<?> property : result.getProperties()) {
                if (property.getName().equals(entry.getKey())) {
                    result = withValue(result, property, entry.getValue());
                }
            }
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState withValue(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map(parsed -> state.setValue(property, parsed)).orElseGet(() -> {
            HarvestRules.LOGGER.warn("harvest_rules:replant: invalid value '{}' for property '{}' of {}", value,
                    property.getName(), state.getBlock());
            return state;
        });
    }
}
