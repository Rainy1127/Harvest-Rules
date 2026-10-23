# Changelog

## 1.0.0 - initial release
- Right-click a mature crop with the main hand to harvest and replant it.
- Default crops: wheat, carrots, potatoes, beetroots, nether wart, cocoa (`#harvest_rules:harvestable`); `#harvest_rules:excluded` always wins; modded `CropBlock` crops with `auto_detect_crop_blocks`.
- Drops from the loot table with the held tool (Fortune works); one seed pays for the replant (from the drops, else the inventory, else no replant).
- Keeps other block state properties (cocoa facing). Vanilla break particles and sound, arm swing, sculk game event.
- Block data map `harvest_rules:replant` (`seed`, `reset_state`, `age_property`).
- Server config: `require_hoe`, `hoe_durability_cost`, `exhaustion`, `xp_per_harvest`, `auto_detect_crop_blocks`, `respect_protection`.
- Sweet berries untouched, sneaking with an item keeps vanilla behaviour, protection and claim mods respected.
- No mixins, no ticking, no dependencies, server side only. English and Russian translations.
