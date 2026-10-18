# Modrinth listing texts

## Project fields

- **Name:** Vein Expansion
- **URL slug:** vein-expansion
- **Summary (max 256 chars):** Veinminer addon: ready-made vein groups for stones, andesite and every log, plus your own blocks and whole mods (Create, Nature's Spirit, Biomes O' Plenty and more), with an in-game config screen.
- **Project type:** Mod
- **Categories:** Utility, Game mechanics
- **Environments:** Client: optional (only needed for the config screen) / Server: required
- **License:** MIT
- **Loaders:** NeoForge
- **Game versions:** 1.21.1, 1.21.11, 26.1, 26.1.1, 26.1.2, 26.2, 26.3
- **Dependencies:** Veinminer (required), Kotlin Lang Forge (required, pulled in by Veinminer)
- **Tags / links:** Source and issues: add your repository URL when you have one

## Description (Modrinth body, Markdown)

# Vein Expansion

**An addon for [Veinminer](https://modrinth.com/mod/veinminer) by Miraculixx. Made by Renterq.**

Veinminer ships with one group: ores. Vein Expansion adds the rest, so you can chop a whole tree, clear a whole patch of stone or mine an entire mod's blocks with a single break. No setup needed: install it next to Veinminer and the groups appear.

## What it adds

| Group | Tool | Blocks |
|---|---|---|
| Andesite | Pickaxe | Andesite and Create's andesite building blocks (cut, polished, bricks, stairs, slabs, walls) |
| Stones | Pickaxe | Every stone type: stone, granite, diorite, andesite, tuff, deepslate, cobblestone, basalt, blackstone, netherrack, end stone and Create's natural stones (limestone, scoria, scorchia, ochrum, crimsite, veridium, asurine) |
| Logs | Axe | Every log, wood, stem and hyphae: vanilla, Nature's Spirit, Biomes O' Plenty and any mod that uses the `minecraft:logs` tag |
| Custom | Your choice | Any block or block tag you add yourself |
| Mods | Any tool | All blocks of the mods you pick, or of every mod. One group per mod, so a Create chain stays in Create |

Chests, machines and other blocks with a block entity are skipped by default, so you cannot chain-break a base by accident. You can turn that off.

## Config screen

Open it from the Mods menu. Three tabs, laid out like DuraHUD's settings:

- **General:** switch Andesite, All stones and All logs on or off.
- **Blocks:** type a block id (`minecraft:dirt`) or tag (`#minecraft:logs`), or search the list of every block in the game and click to add. Pick the tool: pickaxe, axe, shovel, hoe or any.
- **Mods:** "All mods", or pick individual mods from a searchable list.

Settings live in `config/vein_expansion.json` and can also be edited by hand. Cooldown, max blocks, sneaking and everything else about how veinmining feels stay in Veinminer's own settings (`/veinminer`).

After changing the mod list, run `/reload` in the world.

## Requirements

- [Veinminer](https://modrinth.com/mod/veinminer) (it needs Kotlin Lang Forge)
- NeoForge for your Minecraft version
- Install on the server. For the config screen, install on the client too.

## Supported versions

NeoForge 1.21.1, 1.21.11, 26.1.x, 26.2 and 26.3. There is no 1.20.1 build: Veinminer has no mod for Forge or NeoForge 1.20.1 to extend.

## Credits

Vein Expansion only adds groups through Veinminer's config; it does not include or modify Veinminer. All vein mining is done by Veinminer, created by Miraculixx and Max Bossing.

## Version changelog (1.1.0)

**Vein Expansion 1.1.0**

- New: in-game config screen (General / Blocks / Mods tabs).
- New: add any block or block tag of your choice, with a searchable block picker and a tool selector.
- New: add whole mods, one group per mod, or all mods at once. Chests and machines are skipped by default.
- New: support for NeoForge 1.21.11, 26.1, 26.2 and 26.3, besides 1.21.1.
- Changed: settings moved from `config/vein_expansion-common.toml` to `config/vein_expansion.json`. Old settings are not carried over.
- Fixed: plant stems from Nature's Spirit (lotus, desert turnip) were counted as logs.
