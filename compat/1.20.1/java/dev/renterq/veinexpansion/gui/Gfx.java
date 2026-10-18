package dev.renterq.veinexpansion.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Thin wrapper over the game's GUI graphics class, which was renamed in 26.1. */
final class Gfx {
    private final GuiGraphics graphics;

    Gfx(GuiGraphics graphics) {
        this.graphics = graphics;
    }

    void centered(Font font, Component text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, color);
    }

    void centered(Font font, FormattedCharSequence text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, color);
    }
}
