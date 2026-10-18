package dev.renterq.veinexpansion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.renterq.veinexpansion.VeinExpansion;
import dev.renterq.veinexpansion.platform.PlatformHelper;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings in {@code config/vein_expansion.json}. Edited in-game through the config screen
 * (Mods menu) or by hand.
 */
public final class VeinExpansionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static VeinExpansionConfig instance;

    /** Bumped on every save so cached data can tell when it is stale. */
    private static int revision;

    // --- built-in groups
    public boolean enabled = true;
    public boolean andesite = true;
    public boolean stones = true;
    public boolean logs = true;

    // --- your own blocks
    /** Block ids ({@code minecraft:stone}) or block tags ({@code #minecraft:logs}). */
    public List<String> customBlocks = new ArrayList<>();
    public ToolKind customTool = ToolKind.PICKAXE;

    // --- whole mods
    /** Every block of every non-vanilla mod, one group per mod. */
    public boolean allMods = false;
    /** Namespaces (mod ids) whose blocks are always added, e.g. {@code create}. */
    public List<String> modsSelected = new ArrayList<>();
    /** Leave out chests, machines and other blocks that have a block entity. */
    public boolean modsExcludeBlockEntities = true;

    // --- fine tuning (config file only)
    /** Namespaces starting with one of these are scanned for andesite building blocks. */
    public List<String> createNamespaces = new ArrayList<>(List.of("create"));
    /** Andesite blocks whose id contains one of these words are skipped (machine parts). */
    public List<String> andesiteExclude = new ArrayList<>(List.of("casing", "alloy", "scaffold", "ladder",
            "door", "funnel", "tunnel", "shaft", "cogwheel", "belt", "chute", "button", "pressure_plate",
            "lamp", "table_cloth"));
    /** Mods whose log/wood blocks are added by id even if they forgot the minecraft:logs tag. */
    public List<String> woodNamespaces = new ArrayList<>(List.of("natures_spirit", "biomesoplenty"));

    public static synchronized VeinExpansionConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static int revision() {
        return revision;
    }

    private static Path file() {
        return PlatformHelper.configDir().resolve(VeinExpansion.MOD_ID + ".json");
    }

    private static VeinExpansionConfig load() {
        Path file = file();
        VeinExpansionConfig loaded = null;
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(reader, VeinExpansionConfig.class);
            } catch (Exception e) {
                VeinExpansion.LOGGER.warn("Could not read {}, using defaults: {}", file.getFileName(), e.toString());
            }
        }
        if (loaded == null) {
            loaded = new VeinExpansionConfig();
        }
        loaded.sanitize();
        if (!Files.exists(file)) {
            loaded.save();
        }
        return loaded;
    }

    /** Gson leaves missing fields at their defaults but explicit {@code null}s stay null. */
    private void sanitize() {
        if (customBlocks == null) customBlocks = new ArrayList<>();
        if (customTool == null) customTool = ToolKind.PICKAXE;
        if (modsSelected == null) modsSelected = new ArrayList<>();
        if (createNamespaces == null) createNamespaces = new ArrayList<>();
        if (andesiteExclude == null) andesiteExclude = new ArrayList<>();
        if (woodNamespaces == null) woodNamespaces = new ArrayList<>();
    }

    public synchronized void save() {
        revision++;
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            VeinExpansion.LOGGER.error("Could not save {}", file, e);
        }
    }

    public void resetToDefaults() {
        VeinExpansionConfig defaults = new VeinExpansionConfig();
        enabled = defaults.enabled;
        andesite = defaults.andesite;
        stones = defaults.stones;
        logs = defaults.logs;
        customBlocks = defaults.customBlocks;
        customTool = defaults.customTool;
        allMods = defaults.allMods;
        modsSelected = defaults.modsSelected;
        modsExcludeBlockEntities = defaults.modsExcludeBlockEntities;
    }
}
