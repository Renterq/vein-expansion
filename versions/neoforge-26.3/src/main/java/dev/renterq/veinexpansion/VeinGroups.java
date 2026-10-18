package dev.renterq.veinexpansion;

import de.miraculixx.veinminer.ActiveConfig;
import de.miraculixx.veinminer.ConfigBridge;
import de.miraculixx.veinminer.data.BlockGroup;
import de.miraculixx.veinminer.data.VeinminerSettingsOverride;
import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import dev.renterq.veinexpansion.groups.GeneratedPack;
import dev.renterq.veinexpansion.groups.GroupBuilder;
import dev.renterq.veinexpansion.groups.GroupDef;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Hands the Vein Expansion groups to Veinminer. */
public final class VeinGroups {
    /** Folder of the data pack that holds the per-mod block tags. */
    public static Path packDir() {
        return FMLPaths.CONFIGDIR.get().resolve(VeinExpansion.MOD_ID).resolve("generated_pack");
    }

    public static void writePack() {
        int format = SharedConstants.DATA_PACK_FORMAT_MAJOR;
        GeneratedPack.write(packDir(),
                "{\"pack\":{\"min_format\":" + format + ",\"max_format\":" + format
                        + ",\"description\":\"Vein Expansion - generated mod block tags\"}}",
                "block");
    }

    /** Config screen closed: regenerate the tags and, if a world is running, update its groups. */
    public static void refresh() {
        writePack();
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(VeinGroups::apply);
        }
    }

    public static void apply() {
        ConfigBridge bridge = ActiveConfig.INSTANCE.getBridge();
        if (bridge == null) {
            VeinExpansion.LOGGER.warn("Veinminer config bridge is not ready, skipping group injection");
            return;
        }

        Set<BlockGroup<String>> groups = bridge.getGroupsRaw();
        groups.removeIf(group -> group.getName().startsWith(GroupBuilder.PREFIX));

        List<GroupDef> defs = GroupBuilder.build(VeinExpansionConfig.get());
        for (GroupDef def : defs) {
            groups.add(new BlockGroup<>(def.name(), def.blocks(), def.tools(), new VeinminerSettingsOverride()));
        }

        // false = keep the in-memory raw groups, only re-parse them and sync clients.
        bridge.reload(false);
        bridge.save();
        VeinExpansion.LOGGER.info("Injected {} Vein Expansion groups into Veinminer", defs.size());
    }

    private VeinGroups() {}
}
