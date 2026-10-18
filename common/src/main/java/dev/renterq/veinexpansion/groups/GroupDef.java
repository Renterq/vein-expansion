package dev.renterq.veinexpansion.groups;

import java.util.Set;

/**
 * A group of blocks that veinmine together.
 *
 * @param name   unique group name (always starts with {@link GroupBuilder#PREFIX})
 * @param blocks block ids ({@code minecraft:stone}) and block tags ({@code #minecraft:logs})
 * @param tools  item tags ({@code #minecraft:pickaxes}) of the tools that may veinmine the group
 */
public record GroupDef(String name, Set<String> blocks, Set<String> tools) {}
