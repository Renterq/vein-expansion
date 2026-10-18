package dev.renterq.veinexpansion.gui;

import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Pick any registered block (vanilla or modded) to add to your own veinmine group. */
public class BlockPickerScreen extends PagedPickerScreen {
    private record Candidate(String id, Component name, String searchText) {}

    private final VeinExpansionConfig config = VeinExpansionConfig.get();
    private final List<Candidate> all = new ArrayList<>();

    public BlockPickerScreen(Screen parent) {
        super(parent, Component.translatable("vein_expansion.picker.blocks_title"));
        for (Block block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (block == Blocks.AIR) continue;
            Component name = block.getName();
            all.add(new Candidate(id.toString(), name, (id + " " + name.getString()).toLowerCase()));
        }
        all.sort((a, b) -> a.id().compareTo(b.id()));
    }

    @Override
    protected List<Entry> entries(String query) {
        List<Entry> entries = new ArrayList<>();
        for (Candidate candidate : all) {
            if (!query.isEmpty() && !candidate.searchText().contains(query)) continue;
            entries.add(new Entry(candidate.name(), candidate.id(), config.customBlocks.contains(candidate.id()), () -> {
                if (!config.customBlocks.remove(candidate.id())) {
                    config.customBlocks.add(candidate.id());
                }
            }));
        }
        return entries;
    }

    @Override
    protected Component counter() {
        return Component.translatable("vein_expansion.picker.blocks_count", config.customBlocks.size());
    }
}
