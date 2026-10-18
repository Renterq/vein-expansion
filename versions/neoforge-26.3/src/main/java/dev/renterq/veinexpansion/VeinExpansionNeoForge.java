package dev.renterq.veinexpansion;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PathPackResources;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Vein Expansion by Renterq - an addon for Veinminer (Miraculixx).
 *
 * <p>Veinminer reloads its config from disk when a server starts, so the groups are injected at
 * the lowest priority, right after that reload.
 */
@Mod(VeinExpansion.MOD_ID)
public final class VeinExpansionNeoForge {
    public VeinExpansionNeoForge(IEventBus modBus) {
        modBus.addListener(VeinExpansionNeoForge::addPackFinders);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, (ServerStartingEvent event) -> VeinGroups.apply());
    }

    /** Offers the generated per-mod block tags to every world, always on. */
    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        VeinGroups.writePack();
        var location = new PackLocationInfo(VeinExpansion.MOD_ID + "_generated",
                Component.literal("Vein Expansion tags"), PackSource.BUILT_IN, Optional.empty());
        var resources = new PathPackResources.PathResourcesSupplier(VeinGroups.packDir());
        var pack = Pack.readMetaAndCreate(location, resources, PackType.SERVER_DATA,
                new PackSelectionConfig(true, Pack.Position.TOP, false));
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }
}
