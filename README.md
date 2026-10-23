# Harvest Rules

Right-click a mature crop to harvest it and replant it in one go. Server side, zero dependencies, configured with
tags, a data map and a server config.

* Minecraft 26.1.2 / NeoForge 26.1.2.107 / Java 25 / ModDevGradle 2.0.148
* Mod id: `harvest_rules` - package `dev.harvestrules` - version 1.0.0 - MIT
* No mixins, no ticking, no blocks/items/packets, no library mods. Clients do not need it.

## What it does

Right-clicking a crop with the **main hand**, on the server:

1. **Which blocks**: in `#harvest_rules:harvestable` (default: wheat, carrots, potatoes, beetroots, nether wart, cocoa),
   or any block extending vanilla's `CropBlock` when `auto_detect_crop_blocks = true` (most modded crops).
   `#harvest_rules:excluded` always wins. **Sweet berry bushes are never touched** (vanilla already harvests them by
   right click).
2. **Only when mature**: the growth property (`age`) must be at its maximum (`CropBlock#isMaxAge` for crop blocks).
   Immature crops are left alone and the normal interaction goes on (bone meal still works).
3. **Sneaking with an item in the hand** keeps the vanilla behaviour (use the item). Sneaking with an empty hand harvests.
4. **Harvest**: drops come from the block's loot table with the held tool, so **Fortune on a hoe works** exactly like
   breaking the crop. One seed is taken out of the drops to pay for the replant (see below). The crop goes back to
   age 0, all other properties stay (the cocoa pod keeps its facing). Break particles and the vanilla break sound play,
   the arm swings, sculk sensors hear it.

### Where the seed comes from

1. From the drops (normal case). The seed is the block's own item - wheat seeds, carrot, potato, beetroot seeds,
   nether wart, cocoa beans - or the data map's `seed`.
2. If the drops have none (wheat can roll zero seeds), one seed from the player's inventory.
3. If there is none there either, the crop is harvested but **not** replanted - just like breaking it by hand.

So there is never a free replant in survival and no crop can turn into an endless source. Creative players always replant.

## Config

`<world>/serverconfig/harvest_rules-server.toml` (also in Mods -> Harvest Rules -> Config; read on every harvest)

| Key | Default | Meaning |
|---|---|---|
| `auto_detect_crop_blocks` | `true` | Also harvest every `CropBlock` subclass, not only the tag. |
| `require_hoe` | `false` | Only harvest with a hoe (`#minecraft:hoes`) in the main hand. |
| `hoe_durability_cost` | `1` | 0-100. Durability a held hoe loses per harvest (Unbreaking applies). |
| `exhaustion` | `0.005` | 0-10. Food exhaustion per harvest (same as breaking a block). |
| `xp_per_harvest` | `0` | 0-100. Experience points per harvest. |
| `respect_protection` | `true` | Treat a harvest like breaking the block for permission checks (see below). |

## Tags and data map

| Name | Type | Default |
|---|---|---|
| `#harvest_rules:harvestable` | block tag | wheat, carrots, potatoes, beetroots, nether wart, cocoa |
| `#harvest_rules:excluded` | block tag | torchflower crop, pitcher crop, sweet berry bush |
| `harvest_rules:replant` | block data map | empty |

The data map is for crops that do not follow the vanilla pattern. All fields are optional:

```json
// data/harvest_rules/data_maps/block/replant.json
{
  "values": {
    "somemod:pepper_crop": {
      "seed": "somemod:pepper_seeds",
      "reset_state": { "age": "0", "half": "lower" },
      "age_property": "age"
    }
  }
}
```
* `seed` - item taken from the drops for the replant (default: the block's own item).
* `reset_state` - properties after replanting, by name (default: the growth property at its minimum).
* `age_property` - name of the integer growth property (default `age`).

The block still has to be harvestable (tag or auto-detect); the data map only says *how*. `testpack/harvest_rules_test`
is a datapack that excludes potatoes and replants beetroots at age 1, for testing.

## Protection

* Spectators and spawn protection (`mayInteract`): never harvests.
* `respect_protection = true` (default) runs NeoForge's block-break check before harvesting: adventure mode is
  refused, and claim and protection mods that guard block breaking also guard harvesting. Mods that cancel the right click itself already run before Harvest Rules
  (it listens at `LOW` priority and ignores cancelled clicks).
* Side effect of `respect_protection`: mods that react to block breaking (statistics, quests, vein mining) see a
  harvest as a break of that crop. If one of them misbehaves, set `respect_protection = false`.

## What is deliberately left alone

* Sweet berries (vanilla right-click harvest), off-hand clicks, immature crops, sneaking with an item.
* Stems (melon/pumpkin), sugar cane, cactus, bamboo, kelp, cave vines: they are not "harvest and replant" crops.
* Torchflower and pitcher crops (they turn into flowers rather than producing a harvest).
* Client side: nothing runs there.

## Performance

One `RightClickBlock` listener. For a click that is not on a crop it costs a hand check, a side check and two tag
lookups. A harvest is one loot table roll and one block update - the same work as breaking and replanting by hand.
No ticking, no state.

## Installing

Server only (single player: the integrated server is the server). Vanilla clients can join and harvest: the swing and
sound come from the server. Installing on a client only adds the config screen.

## Build

```
./gradlew build          # jar in build/libs
./gradlew runClient
./gradlew runServer      # accept the EULA in run/eula.txt first
./gradlew test           # HarvestGate and SeedLogic unit tests
```

## Layout

```
src/main/java/dev/harvestrules/
  HarvestRules.java         common entrypoint: server config, data map, listener, tag keys
  HarvestRulesClient.java   client-only entrypoint: config screen
  HarvestConfig.java        ModConfigSpec (server)
  HarvestDataMaps.java      harvest_rules:replant
  HarvestHandler.java       the RightClickBlock listener
  logic/                    pure Java, unit tested: HarvestGate, SeedLogic, HarvestSettings
src/main/resources/data/harvest_rules/tags/block/{harvestable,excluded}.json
src/main/resources/assets/harvest_rules/lang/{en_us,ru_ru}.json
src/main/templates/META-INF/neoforge.mods.toml
src/test/java/dev/harvestrules/logic/*Test.java
testpack/harvest_rules_test/   test datapack
art/                           icon_400.png (A), icon_400_name.png (B) and their SVG sources
```

Written from scratch; other harvest mods were only compared by their public descriptions.
