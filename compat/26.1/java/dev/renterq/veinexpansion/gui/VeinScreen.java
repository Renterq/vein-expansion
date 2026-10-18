package dev.renterq.veinexpansion.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Base of the config screens; hides that rendering was renamed in 26.1. */
abstract class VeinScreen extends Screen {
    protected VeinScreen(Component title) {
        super(title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        drawOverlay(new Gfx(graphics), mouseX, mouseY, delta);
    }

    /** Shows another screen (the method that does it was renamed in 26.2). */
    protected void openScreen(Screen screen) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(screen);
        }
    }

    /** Drawn on top of the widgets. */
    protected abstract void drawOverlay(Gfx gfx, int mouseX, int mouseY, float delta);
}
