package dev.renterq.veinexpansion;

import dev.renterq.veinexpansion.gui.VeinConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/** Client half: the config button in the mod list. Only loaded on the client. */
final class VeinExpansionClient {
    static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new VeinConfigScreen(parent)));
    }

    private VeinExpansionClient() {}
}
