package dev.renterq.veinexpansion.platform;

import dev.renterq.veinexpansion.VeinGroups;
import java.nio.file.Path;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

/** Loader specific bits the shared code needs (NeoForge 1.21.1). */
public final class PlatformHelper {
    public static Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static String modName(String namespace) {
        if ("minecraft".equals(namespace)) return "Minecraft";
        return ModList.get().getModContainerById(namespace)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(namespace);
    }

    /** Called after the config screen saved: rebuild the tag pack and re-inject the groups. */
    public static void onConfigSaved() {
        VeinGroups.refresh();
    }

    private PlatformHelper() {}
}
