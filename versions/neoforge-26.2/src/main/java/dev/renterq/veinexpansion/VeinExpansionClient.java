package dev.renterq.veinexpansion;

import dev.renterq.veinexpansion.gui.VeinConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client half: the config button in the mod list. */
@Mod(value = VeinExpansion.MOD_ID, dist = Dist.CLIENT)
public final class VeinExpansionClient {
    public VeinExpansionClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> new VeinConfigScreen(parent));
    }
}
