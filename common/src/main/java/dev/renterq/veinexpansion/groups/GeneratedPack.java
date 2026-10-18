package dev.renterq.veinexpansion.groups;

import dev.renterq.veinexpansion.VeinExpansion;
import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Writes the data pack that holds one block tag per selected mod
 * ({@code vein_expansion:mod/<namespace>}), so Veinminer's groups only need to name the tag
 * instead of listing every block.
 */
public final class GeneratedPack {
    /**
     * @param packMeta   full contents of {@code pack.mcmeta} (its fields change between game versions)
     * @param tagFolder  {@code blocks} before 1.21, {@code block} from 1.21
     */
    public static void write(Path packDir, String packMeta, String tagFolder) {
        VeinExpansionConfig config = VeinExpansionConfig.get();
        try {
            Path tags = packDir.resolve("data").resolve(VeinExpansion.MOD_ID).resolve("tags").resolve(tagFolder).resolve("mod");
            deleteRecursively(packDir.resolve("data"));
            Files.createDirectories(tags);
            Files.writeString(packDir.resolve("pack.mcmeta"), packMeta, StandardCharsets.UTF_8);

            Set<String> namespaces = GroupBuilder.selectedNamespaces(config);
            Map<String, List<String>> blocks = ModBlocks.byNamespace(config.modsExcludeBlockEntities);
            for (String namespace : namespaces) {
                String values = blocks.get(namespace).stream()
                        .map(id -> "\"" + id + "\"")
                        .collect(Collectors.joining(","));
                Files.writeString(tags.resolve(namespace + ".json"),
                        "{\"replace\":false,\"values\":[" + values + "]}", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            VeinExpansion.LOGGER.error("Could not write the generated data pack in {}", packDir, e);
        }
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

    private GeneratedPack() {}
}
