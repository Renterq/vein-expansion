package dev.renterq.veinexpansion.platform;

import dev.renterq.veinexpansion.FunctionPack;
import java.nio.file.Path;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

/** Loader specific bits the shared code needs (Forge 1.20.1). */
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

    /** Called after the config screen saved: rewrite the generated data pack (applies on /reload). */
    public static void onConfigSaved() {
        FunctionPack.write();
    }

    private PlatformHelper() {}
}
