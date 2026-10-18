package dev.renterq.veinexpansion;

import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import dev.renterq.veinexpansion.groups.GroupBuilder;
import dev.renterq.veinexpansion.groups.GroupDef;
import dev.renterq.veinexpansion.groups.ModBlocks;
import dev.renterq.veinexpansion.platform.PlatformHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

/**
 * Generates the data pack behind Vein Expansion on 1.20.1.
 *
 * <p>It works like the Veinminer data pack: one {@code minecraft.mined} scoreboard per block tells
 * which block a player just broke, then a recursive function breaks the connected blocks of the
 * same group with {@code setblock ... air destroy}. Because that is the only way to see a broken
 * block from a data pack, the number of tracked blocks is capped by {@link #MAX_TRACKED}.
 */
public final class FunctionPack {
    /** Scoreboards (tracked blocks) per world; every one costs a few commands each tick. */
    static final int MAX_TRACKED = 1500;
    /** Blocks one break may take, the origin included. */
    static final int MAX_CHAIN = 100;

    private static final String NS = VeinExpansion.MOD_ID;
    private static final List<String> LOG_SUFFIXES = List.of("_log", "_wood", "_stem", "_hyphae");

    public static Path packDir() {
        return PlatformHelper.configDir().resolve(NS).resolve("generated_pack");
    }

    public static void write() {
        Path root = packDir();
        try {
            deleteRecursively(root.resolve("data"));
            Path data = root.resolve("data").resolve(NS);
            Files.createDirectories(data.resolve("functions"));
            Files.createDirectories(data.resolve("tags").resolve("blocks"));
            Files.createDirectories(data.resolve("tags").resolve("functions"));
            Files.createDirectories(data.resolve("predicates"));
            Path mcTags = root.resolve("data").resolve("minecraft").resolve("tags").resolve("functions");
            Files.createDirectories(mcTags);

            put(root.resolve("pack.mcmeta"),
                    "{\"pack\":{\"pack_format\":15,\"description\":\"Vein Expansion generated groups\"}}");
            put(mcTags.resolve("load.json"), "{\"values\":[\"" + NS + ":load\"]}");
            put(mcTags.resolve("tick.json"), "{\"values\":[\"" + NS + ":tick\"]}");

            generate(data);
        } catch (IOException e) {
            VeinExpansion.LOGGER.error("Could not write the generated data pack in {}", root, e);
        }
    }

    private static void generate(Path data) throws IOException {
        VeinExpansionConfig config = VeinExpansionConfig.get();

        // Group name -> {file id, blocks, tool predicate}
        Map<String, Group> groups = new LinkedHashMap<>();
        for (GroupDef def : GroupBuilder.build(config)) {
            Set<String> blocks = resolve(def);
            if (blocks.isEmpty()) continue;
            String id = fileId(def.name());
            groups.put(def.name(), new Group(id, blocks, predicateFor(data, def.tools())));
        }

        // Each block starts the first group it belongs to.
        Map<String, Group> starter = new LinkedHashMap<>();
        for (Group group : groups.values()) {
            for (String block : group.blocks) {
                if (starter.size() >= MAX_TRACKED) break;
                starter.putIfAbsent(block, group);
            }
        }
        int total = groups.values().stream().mapToInt(g -> g.blocks.size()).sum();
        if (starter.size() >= MAX_TRACKED) {
            VeinExpansion.LOGGER.warn("More than {} blocks selected ({} entries): only the first {} start a vein "
                    + "(the 1.20.1 data pack cannot track more). Pick fewer mods.", MAX_TRACKED, total, MAX_TRACKED);
        }

        List<String> load = new ArrayList<>();
        load.add("scoreboard objectives add ve_cd dummy");
        load.add("scoreboard objectives add ve_set dummy");
        List<String> tick = new ArrayList<>();
        tick.add("execute as @a unless score @s ve_cd matches 0.. run scoreboard players set @s ve_cd 0");
        tick.add("execute as @a[scores={ve_cd=1..}] run scoreboard players remove @s ve_cd 1");
        List<String> resets = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Group> entry : starter.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id == null || id.getPath().contains("/")) continue;
            String objective = "ve" + index++;
            Group group = entry.getValue();
            load.add("scoreboard objectives add " + objective + " minecraft.mined:" + id.getNamespace() + "."
                    + id.getPath());
            tick.add("execute as @a[scores={" + objective + "=1..,ve_cd=..0},predicate=" + group.predicate
                    + "] at @s anchored eyes positioned ^ ^ ^1.5 run function " + NS + ":start/" + group.id);
            resets.add("scoreboard players set @a " + objective + " 0");
        }
        tick.addAll(resets);
        put(data.resolve("functions").resolve("load.mcfunction"), String.join("\n", load) + "\n");
        put(data.resolve("functions").resolve("tick.mcfunction"), String.join("\n", tick) + "\n");

        Files.createDirectories(data.resolve("functions").resolve("start"));
        Files.createDirectories(data.resolve("functions").resolve("rec"));
        for (Group group : groups.values()) {
            String tag = "#" + NS + ":" + group.id;
            put(data.resolve("tags").resolve("blocks").resolve(group.id + ".json"),
                    "{\"replace\":false,\"values\":[" + group.blocks.stream()
                            .map(block -> "\"" + block + "\"").collect(Collectors.joining(",")) + "]}");

            put(data.resolve("functions").resolve("start").resolve(group.id + ".mcfunction"), String.join("\n",
                    "scoreboard players set #n ve_set 0",
                    "execute if score hunger settings matches 1.. run effect give @s minecraft:hunger 2 120 true",
                    "execute at @e[type=item,limit=1,sort=nearest,nbt={Age:0s}] positioned ~ ~ ~ run function "
                            + NS + ":rec/" + group.id,
                    "scoreboard players set @s ve_cd 10",
                    "execute if score default cooldown matches 0.. run scoreboard players operation @s ve_cd = default cooldown",
                    ""));

            String recurse = "execute if score #n ve_set matches ..%d positioned %s if block ~ ~ ~ %s run function %s:rec/%s";
            List<String> rec = new ArrayList<>();
            rec.add("scoreboard players add #n ve_set 1");
            rec.add("execute if block ~ ~ ~ " + tag + " run setblock ~ ~ ~ air destroy");
            for (String offset : List.of("~ ~ ~1", "~ ~ ~-1", "~1 ~ ~", "~-1 ~ ~", "~ ~1 ~", "~ ~-1 ~")) {
                rec.add(String.format(recurse, MAX_CHAIN - 1, offset, tag, NS, group.id));
            }
            put(data.resolve("functions").resolve("rec").resolve(group.id + ".mcfunction"),
                    String.join("\n", rec) + "\n");
        }
        VeinExpansion.LOGGER.info("Generated {} Vein Expansion groups tracking {} blocks", groups.size(), starter.size());
    }

    /** Explicit, registered block ids of a group. Tags cannot be used: this runs before tags are loaded. */
    private static Set<String> resolve(GroupDef def) {
        VeinExpansionConfig config = VeinExpansionConfig.get();
        Set<String> blocks = new LinkedHashSet<>();
        for (String entry : def.blocks()) {
            if (entry.startsWith("#" + NS + ":mod/")) {
                String namespace = entry.substring(entry.indexOf("mod/") + 4);
                blocks.addAll(ModBlocks.byNamespace(config.modsExcludeBlockEntities)
                        .getOrDefault(namespace, List.of()));
            } else if (entry.equals("#minecraft:logs")) {
                blocks.addAll(scanLogs());
            } else if (entry.startsWith("#")) {
                if (def.name().equals(GroupBuilder.CUSTOM)) {
                    VeinExpansion.LOGGER.warn("Block tag {} is not supported on 1.20.1, use block ids", entry);
                }
            } else {
                ResourceLocation id = ResourceLocation.tryParse(entry);
                if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) blocks.add(id.toString());
            }
        }
        return blocks;
    }

    /** Every log, wood, stem and hyphae of every mod (they are all pillar blocks). */
    private static Set<String> scanLogs() {
        Set<String> logs = new LinkedHashSet<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof RotatedPillarBlock)) continue;
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (LOG_SUFFIXES.stream().anyMatch(id.getPath()::endsWith)) logs.add(id.toString());
        }
        return logs;
    }

    /** Writes the "holding one of these tools" predicate (once) and returns its function-friendly id. */
    private static String predicateFor(Path data, Set<String> toolTags) throws IOException {
        List<String> tags = toolTags.stream().map(tag -> tag.substring(1)).toList();
        String name = "tool_" + tags.stream().map(tag -> tag.substring(tag.indexOf(':') + 1)).collect(Collectors.joining("_"));
        List<String> terms = new ArrayList<>();
        for (String tag : tags) {
            terms.add("{\"condition\":\"minecraft:entity_properties\",\"entity\":\"this\",\"predicate\":"
                    + "{\"equipment\":{\"mainhand\":{\"tag\":\"" + tag + "\"}}}}");
        }
        String json = terms.size() == 1 ? terms.get(0)
                : "{\"condition\":\"minecraft:alternative\",\"terms\":[" + String.join(",", terms) + "]}";
        put(data.resolve("predicates").resolve(name + ".json"), json);
        return NS + ":" + name;
    }

    private static String fileId(String groupName) {
        return groupName.substring(GroupBuilder.PREFIX.length()).toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    private static void put(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(dir)) {
            paths = new ArrayList<>(walk.toList());
        }
        for (int i = paths.size() - 1; i >= 0; i--) {
            Files.deleteIfExists(paths.get(i));
        }
    }

    private record Group(String id, Set<String> blocks, String predicate) {}

    private FunctionPack() {}
}
