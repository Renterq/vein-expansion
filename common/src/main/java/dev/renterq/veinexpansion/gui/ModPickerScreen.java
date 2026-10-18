package dev.renterq.veinexpansion.gui;

import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import dev.renterq.veinexpansion.groups.ModBlocks;
import dev.renterq.veinexpansion.platform.PlatformHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Pick the mods whose blocks all veinmine (one group per mod). */
public class ModPickerScreen extends PagedPickerScreen {
    private record Candidate(String namespace, String name, int blocks) {}

    private final VeinExpansionConfig config = VeinExpansionConfig.get();
    private final List<Candidate> all = new ArrayList<>();

    public ModPickerScreen(Screen parent) {
        super(parent, Component.translatable("vein_expansion.picker.mods_title"));
        Map<String, List<String>> byNamespace = ModBlocks.byNamespace(config.modsExcludeBlockEntities);
        byNamespace.forEach((namespace, blocks) ->
                all.add(new Candidate(namespace, PlatformHelper.modName(namespace), blocks.size())));
    }

    @Override
    protected List<Entry> entries(String query) {
        List<Entry> entries = new ArrayList<>();
        for (Candidate candidate : all) {
            if (!query.isEmpty() && !(candidate.namespace() + " " + candidate.name()).toLowerCase().contains(query)) continue;
            boolean selected = config.modsSelected.contains(candidate.namespace());
            entries.add(new Entry(Component.literal(candidate.name()),
                    candidate.namespace() + " - " + candidate.blocks() + " blocks", selected, () -> {
                        if (!config.modsSelected.remove(candidate.namespace())) {
                            config.modsSelected.add(candidate.namespace());
                        }
                    }));
        }
        return entries;
    }

    @Override
    protected Component counter() {
        return Component.translatable("vein_expansion.picker.mods_count", config.modsSelected.size());
    }
}
