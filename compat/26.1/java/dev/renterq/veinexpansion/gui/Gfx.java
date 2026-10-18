package dev.renterq.veinexpansion.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Thin wrapper over the game's GUI graphics class, which was renamed in 26.1. */
final class Gfx {
    private final GuiGraphicsExtractor graphics;

    Gfx(GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
    }

    void centered(Font font, Component text, int x, int y, int color) {
        graphics.centeredText(font, text, x, y, color);
    }

    void centered(Font font, FormattedCharSequence text, int x, int y, int color) {
        graphics.centeredText(font, text, x, y, color);
    }
}
