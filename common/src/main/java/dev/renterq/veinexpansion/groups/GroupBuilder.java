package dev.renterq.veinexpansion.groups;

import dev.renterq.veinexpansion.VeinExpansion;
import dev.renterq.veinexpansion.config.ToolKind;
import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Turns the config into the list of Veinminer groups. */
public final class GroupBuilder {
    public static final String PREFIX = "Renterq-";
    public static final String ANDESITE = PREFIX + "Andesite";
    public static final String STONES = PREFIX + "Stones";
    public static final String LOGS = PREFIX + "Logs";
    public static final String CUSTOM = PREFIX + "Custom";
    public static final String MOD_PREFIX = PREFIX + "Mod-";

    /** Block tags generated per mod, e.g. {@code #vein_expansion:mod/create}. */
    public static String modTag(String namespace) {
        return "#" + VeinExpansion.MOD_ID + ":mod/" + namespace;
    }

    private static final List<String> PICKAXES = List.of("#minecraft:pickaxes");
    private static final List<String> AXES = List.of("#minecraft:axes");

    /** Create's natural stones (Create 6.x). Unknown ids are dropped by Veinminer. */
    private static final List<String> CREATE_STONES = List.of(
            "create:limestone", "create:scoria", "create:scorchia",
            "create:ochrum", "create:crimsite", "create:veridium", "create:asurine");

    private static final List<String> VANILLA_STONES = List.of(
            "#c:stones", "#c:cobblestones", "#minecraft:base_stone_overworld", "#minecraft:base_stone_nether",
            "minecraft:stone", "minecraft:granite", "minecraft:diorite", "minecraft:andesite", "minecraft:tuff",
            "minecraft:deepslate", "minecraft:cobblestone", "minecraft:mossy_cobblestone",
            "minecraft:cobbled_deepslate", "minecraft:calcite", "minecraft:dripstone_block",
            "minecraft:basalt", "minecraft:smooth_basalt", "minecraft:blackstone", "minecraft:netherrack",
            "minecraft:end_stone");

    private static final List<String> VANILLA_ANDESITE = List.of(
            "minecraft:andesite", "minecraft:polished_andesite", "minecraft:andesite_stairs",
            "minecraft:andesite_slab", "minecraft:andesite_wall", "minecraft:polished_andesite_stairs",
            "minecraft:polished_andesite_slab");

    private static final List<String> WOOD_SUFFIXES = List.of("_log", "_wood", "_hyphae");

    public static List<GroupDef> build(VeinExpansionConfig config) {
        List<GroupDef> groups = new ArrayList<>();
        if (!config.enabled) return groups;

        if (config.andesite) groups.add(new GroupDef(ANDESITE, andesiteBlocks(config), toolSet(PICKAXES)));
        if (config.stones) groups.add(new GroupDef(STONES, new LinkedHashSet<>(stoneBlocks()), toolSet(PICKAXES)));
        if (config.logs) groups.add(new GroupDef(LOGS, logBlocks(config), toolSet(AXES)));

        Set<String> custom = new LinkedHashSet<>();
        for (String entry : config.customBlocks) {
            if (entry != null && !entry.isBlank()) custom.add(entry.trim());
        }
        if (!custom.isEmpty()) groups.add(new GroupDef(CUSTOM, custom, toolSet(config.customTool.tools())));

        for (String namespace : selectedNamespaces(config)) {
            groups.add(new GroupDef(MOD_PREFIX + namespace, new LinkedHashSet<>(List.of(modTag(namespace))),
                    toolSet(ToolKind.ANY.tools())));
        }
        return groups;
    }

    /** Mods that get a group of all their blocks, in a stable order. */
    public static Set<String> selectedNamespaces(VeinExpansionConfig config) {
        Map<String, List<String>> available = ModBlocks.byNamespace(config.modsExcludeBlockEntities);
        Set<String> wanted = new TreeSet<>(config.modsSelected);
        if (config.allMods) {
            wanted.addAll(available.keySet());
            if (!config.modsSelected.contains("minecraft")) wanted.remove("minecraft");
        }
        wanted.retainAll(available.keySet());
        return wanted;
    }

    private static Set<String> toolSet(List<String> tools) {
        return new LinkedHashSet<>(tools);
    }

    private static Set<String> andesiteBlocks(VeinExpansionConfig config) {
        Set<String> blocks = new LinkedHashSet<>(VANILLA_ANDESITE);
        for (Block block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (!startsWithAny(id.getNamespace(), config.createNamespaces)) continue;
            String path = id.getPath();
            if (!path.contains("andesite") || containsAny(path, config.andesiteExclude)) continue;
            blocks.add(id.toString());
        }
        return blocks;
    }

    private static List<String> stoneBlocks() {
        List<String> blocks = new ArrayList<>(VANILLA_STONES);
        blocks.addAll(CREATE_STONES);
        return blocks;
    }

    private static Set<String> logBlocks(VeinExpansionConfig config) {
        Set<String> blocks = new LinkedHashSet<>();
        blocks.add("#minecraft:logs");
        for (Block block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (!config.woodNamespaces.contains(id.getNamespace())) continue;
            if (block == Blocks.AIR || block.defaultBlockState().is(BlockTags.LOGS)) continue;
            String path = id.getPath();
            if (WOOD_SUFFIXES.stream().anyMatch(path::endsWith)) blocks.add(id.toString());
        }
        return blocks;
    }

    private static boolean startsWithAny(String value, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) return true;
        }
        return false;
    }

    private static boolean containsAny(String value, List<String> words) {
        for (String word : words) {
            if (value.contains(word)) return true;
        }
        return false;
    }

    private GroupBuilder() {}
}
