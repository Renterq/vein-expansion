package dev.renterq.veinexpansion.config;

import java.util.List;
import net.minecraft.network.chat.Component;

/** Which tools may veinmine the custom block group. */
public enum ToolKind {
    PICKAXE("#minecraft:pickaxes"),
    AXE("#minecraft:axes"),
    SHOVEL("#minecraft:shovels"),
    HOE("#minecraft:hoes"),
    ANY("#minecraft:pickaxes", "#minecraft:axes", "#minecraft:shovels", "#minecraft:hoes");

    private final List<String> tools;

    ToolKind(String... tools) {
        this.tools = List.of(tools);
    }

    /** Item tags (as {@code #namespace:tag}) accepted by Veinminer for this kind. */
    public List<String> tools() {
        return tools;
    }

    public Component label() {
        return Component.translatable("vein_expansion.tool." + name().toLowerCase());
    }
}
