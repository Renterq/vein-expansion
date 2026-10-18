package dev.renterq.veinexpansion.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;

/** Registry scans that decide which blocks of a mod are worth veinmining. */
public final class ModBlocks {
    /** Blocks a vein miner should touch: breakable, not air, not fluid, optionally without block entity. */
    public static boolean eligible(Block block, boolean excludeBlockEntities) {
        if (block == Blocks.AIR || block instanceof LiquidBlock) return false;
        if (excludeBlockEntities && block instanceof EntityBlock) return false;
        return block.defaultBlockState().getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) >= 0;
    }

    /** Namespace (mod id) to its eligible block ids (as strings), sorted by namespace then id. */
    public static Map<String, List<String>> byNamespace(boolean excludeBlockEntities) {
        Map<String, List<String>> result = new TreeMap<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!eligible(block, excludeBlockEntities)) continue;
            var id = BuiltInRegistries.BLOCK.getKey(block);
            result.computeIfAbsent(id.getNamespace(), ns -> new ArrayList<>()).add(id.toString());
        }
        result.values().forEach(list -> list.sort(null));
        return result;
    }

    private ModBlocks() {}
}
