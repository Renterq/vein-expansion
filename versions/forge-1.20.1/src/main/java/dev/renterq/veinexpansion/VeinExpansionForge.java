package dev.renterq.veinexpansion;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Vein Expansion for Forge 1.20.1, by Renterq.
 *
 * <p>Veinminer has no real mod for 1.20.1: its only build is a data pack of 18 hard-coded ores.
 * This mod therefore ships its extra groups as a generated data pack that works the same way
 * (mined-block scoreboards plus a recursive function) and reads that data pack's cooldown and
 * hunger settings when they exist. The same jar loads on NeoForge 1.20.1.
 */
@Mod(VeinExpansion.MOD_ID)
public final class VeinExpansionForge {
    public VeinExpansionForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(VeinExpansionForge::addPackFinders);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> VeinExpansionClient::register);
    }

    /** Offers the generated data pack to every world, always on. */
    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        FunctionPack.write();
        String id = VeinExpansion.MOD_ID + "_generated";
        Pack pack = Pack.readMetaAndCreate(id, Component.literal("Vein Expansion groups"), true,
                name -> new PathPackResources(name, FunctionPack.packDir(), false),
                PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        }
    }
}
