# Veinminer jars for compiling

The NeoForge modules compile against Veinminer (by Miraculixx, AGPL-3.0), which is not part of this repository.
Download each jar from [Modrinth](https://modrinth.com/mod/veinminer/versions) and put it in `versions/<module>/libs/`
under the exact name below (`gradle.properties` of the module names it as `veinminer_jar`).

| Module | File |
|---|---|
| `neoforge-1.21.1` | `veinminer-neoforge-2.11.2+1.21.1.jar` |
| `neoforge-1.21.11` | `veinminer-neoforge-2.11.2+1.21.11.jar` |
| `neoforge-26.1` | `veinminer-neoforge-2.10.4.jar` |
| `neoforge-26.2` | `veinminer-neoforge-2.12.1.jar` |
| `neoforge-26.3` | `veinminer-neoforge-2.12.3.jar` |

`forge-1.20.1` needs no Veinminer jar.
