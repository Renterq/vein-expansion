## Vein Expansion 1.1.0

An addon for [Veinminer](https://modrinth.com/mod/veinminer) by Miraculixx. Made by **Renterq**.

Adds ready-made vein groups (andesite, all stones, all logs), your own blocks and whole mods, with an in-game config screen.

### Which file do I download?

| Minecraft | Loader | File |
|---|---|---|
| 1.20.1 | Forge (also loads on NeoForge 1.20.1) | `vein-expansion-1.1.0-forge-1.20.1.jar` |
| 1.21.1 | NeoForge | `vein-expansion-1.1.0-neoforge-1.21.1.jar` |
| 1.21.11 | NeoForge | `vein-expansion-1.1.0-neoforge-1.21.11.jar` |
| 26.1, 26.1.1, 26.1.2 | NeoForge | `vein-expansion-1.1.0-neoforge-26.1.jar` |
| 26.2 | NeoForge | `vein-expansion-1.1.0-neoforge-26.2.jar` |
| 26.3 | NeoForge | `vein-expansion-1.1.0-neoforge-26.3.jar` |

The NeoForge builds need [Veinminer](https://modrinth.com/mod/veinminer) (and Kotlin Lang Forge). The 1.20.1 build needs nothing else: Veinminer has no real mod for 1.20.1, so that build generates a data pack that works the same way (limits: 1500 tracked blocks, 100 blocks per break, block ids instead of tags, no tool wear).

### What's new

- In-game config screen (General / Blocks / Mods tabs).
- Add any block or block tag of your choice, with a searchable block picker and a tool selector.
- Add whole mods, one group per mod, or all mods at once. Chests and machines are skipped by default.
- New builds: Forge 1.20.1, NeoForge 1.21.11, 26.1, 26.2 and 26.3, besides 1.21.1.
- Settings moved from `vein_expansion-common.toml` to `config/vein_expansion.json` (old settings are not carried over).
- Fixed: plant stems from Nature's Spirit (lotus, desert turnip) were counted as logs.
