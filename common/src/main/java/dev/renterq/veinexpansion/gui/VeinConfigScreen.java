package dev.renterq.veinexpansion.gui;

import dev.renterq.veinexpansion.config.ToolKind;
import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import dev.renterq.veinexpansion.platform.PlatformHelper;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Three tabs in the same layout as DuraHUD's settings: general switches, your own blocks and
 * whole mods. Everything is written to {@code config/vein_expansion.json} when the screen closes.
 */
public class VeinConfigScreen extends VeinScreen {
    private static final int OPTION_WIDTH = 150;
    private static final int OPTION_HEIGHT = 20;
    private static final int FIRST_ROW_Y = 52;
    private static final int ROW_STEP = 22;
    private static final int ENTRIES_PER_PAGE = 4;
    private static final int INPUT_Y = FIRST_ROW_Y + ROW_STEP + 2;
    private static final int LIST_Y = INPUT_Y + 26;
    private static final int PAGER_Y = LIST_Y + ENTRIES_PER_PAGE * ROW_STEP;
    private static final Pattern VALID_ENTRY = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_./-]+");

    private static final String[] TAB_KEYS = {
            "vein_expansion.config.tab.general",
            "vein_expansion.config.tab.blocks",
            "vein_expansion.config.tab.mods"
    };

    private final Screen parent;
    private final VeinExpansionConfig config = VeinExpansionConfig.get();
    private int page;
    private int entryPage;
    private String pendingEntry = "";

    public VeinConfigScreen(Screen parent) {
        super(Component.translatable("vein_expansion.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addTabs();

        switch (page) {
            case 1 -> initBlocksPage();
            case 2 -> initModsPage();
            default -> initGeneralPage();
        }

        addRenderableWidget(Button.builder(Component.translatable("vein_expansion.config.reset"), button -> {
            config.resetToDefaults();
            entryPage = 0;
            rebuildWidgets();
        }).bounds(this.width / 2 - 155, this.height - 28, OPTION_WIDTH, OPTION_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 + 5, this.height - 28, OPTION_WIDTH, OPTION_HEIGHT).build());
    }

    private void addTabs() {
        for (int i = 0; i < TAB_KEYS.length; i++) {
            final int target = i;
            Button tab = Button.builder(Component.translatable(TAB_KEYS[i]), button -> {
                page = target;
                rebuildWidgets();
            }).bounds(this.width / 2 - 152 + i * 102, 28, 100, OPTION_HEIGHT).build();
            tab.active = page != i;
            addRenderableWidget(tab);
        }
    }

    // ------------------------------------------------------------------ general

    private void initGeneralPage() {
        addRenderableWidget(toggle(0, "vein_expansion.option.enabled", config.enabled, value -> config.enabled = value));
        addRenderableWidget(toggle(1, "vein_expansion.option.andesite", config.andesite, value -> config.andesite = value));
        addRenderableWidget(toggle(2, "vein_expansion.option.stones", config.stones, value -> config.stones = value));
        addRenderableWidget(toggle(3, "vein_expansion.option.logs", config.logs, value -> config.logs = value));
    }

    // ------------------------------------------------------------------ blocks

    private void initBlocksPage() {
        // A plain button instead of CycleButton: its builder signature differs between versions.
        addRenderableWidget(Button.builder(toolLabel(), button -> {
            ToolKind[] kinds = ToolKind.values();
            config.customTool = kinds[(config.customTool.ordinal() + 1) % kinds.length];
            button.setMessage(toolLabel());
        }).bounds(columnX(0), rowY(0), OPTION_WIDTH, OPTION_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.translatable("vein_expansion.option.pick_blocks"), button -> {
            openScreen(new BlockPickerScreen(this));
        }).bounds(columnX(1), rowY(0), OPTION_WIDTH, OPTION_HEIGHT).build());

        EditBox box = new EditBox(this.font, this.width / 2 - 155, INPUT_Y, 230, OPTION_HEIGHT,
                Component.translatable("vein_expansion.option.entry"));
        box.setMaxLength(100);
        box.setHint(Component.translatable("vein_expansion.option.entry_hint"));
        box.setValue(pendingEntry);
        box.setResponder(value -> pendingEntry = value);
        addRenderableWidget(box);

        addRenderableWidget(Button.builder(Component.translatable("vein_expansion.option.add"), button -> addPendingEntry())
                .bounds(this.width / 2 + 80, INPUT_Y, 75, OPTION_HEIGHT).build());

        List<String> entries = config.customBlocks;
        int pages = Math.max(1, (entries.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
        entryPage = Math.max(0, Math.min(entryPage, pages - 1));
        int start = entryPage * ENTRIES_PER_PAGE;
        int end = Math.min(entries.size(), start + ENTRIES_PER_PAGE);
        for (int i = start; i < end; i++) {
            final String entry = entries.get(i);
            int y = LIST_Y + (i - start) * ROW_STEP;
            Button remove = Button.builder(Component.literal("✘ " + entry), button -> {
                config.customBlocks.remove(entry);
                rebuildWidgets();
            }).bounds(this.width / 2 - 155, y, 310, OPTION_HEIGHT).build();
            remove.setTooltip(Tooltip.create(Component.translatable("vein_expansion.option.remove_hint")));
            addRenderableWidget(remove);
        }

        Button previous = Button.builder(Component.literal("<"), button -> {
            entryPage--;
            rebuildWidgets();
        }).bounds(this.width / 2 - 155, PAGER_Y, 74, OPTION_HEIGHT).build();
        previous.active = entryPage > 0;
        addRenderableWidget(previous);

        Button next = Button.builder(Component.literal(">"), button -> {
            entryPage++;
            rebuildWidgets();
        }).bounds(this.width / 2 + 81, PAGER_Y, 74, OPTION_HEIGHT).build();
        next.active = entryPage < pages - 1;
        addRenderableWidget(next);
    }

    private Component toolLabel() {
        return Component.translatable("vein_expansion.option.tool").append(": ").append(config.customTool.label());
    }

    private void addPendingEntry() {
        String entry = pendingEntry.trim().toLowerCase();
        if (!entry.isEmpty() && !entry.contains(":")) {
            entry = "minecraft:" + entry;
        }
        if (VALID_ENTRY.matcher(entry).matches() && !config.customBlocks.contains(entry)) {
            config.customBlocks.add(entry);
            pendingEntry = "";
            entryPage = Integer.MAX_VALUE;
            rebuildWidgets();
        }
    }

    // ------------------------------------------------------------------ mods

    private void initModsPage() {
        addRenderableWidget(toggle(0, "vein_expansion.option.all_mods", config.allMods, value -> config.allMods = value));
        addRenderableWidget(toggle(1, "vein_expansion.option.exclude_machines", config.modsExcludeBlockEntities,
                value -> config.modsExcludeBlockEntities = value));

        addRenderableWidget(Button.builder(Component.translatable("vein_expansion.option.pick_mods")
                        .append(" (" + config.modsSelected.size() + ")"), button -> {
                    openScreen(new ModPickerScreen(this));
                }).bounds(columnX(2), rowY(2), OPTION_WIDTH, OPTION_HEIGHT).build());
    }

    // ------------------------------------------------------------------ render

    @Override
    protected void drawOverlay(Gfx gfx, int mouseX, int mouseY, float delta) {
        gfx.centered(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

        int hintY = switch (page) {
            case 1 -> PAGER_Y + 26;
            case 2 -> rowY(2) + 34;
            default -> rowY(3) + 34;
        };
        String hintKey = switch (page) {
            case 1 -> "vein_expansion.hint.blocks";
            case 2 -> "vein_expansion.hint.mods";
            default -> "vein_expansion.hint.general";
        };
        int line = 0;
        for (var text : this.font.split(Component.translatable(hintKey), 300)) {
            gfx.centered(this.font, text, this.width / 2, hintY + line * 11, 0xFFA0A0A0);
            line++;
        }
    }

    @Override
    public void onClose() {
        config.save();
        PlatformHelper.onConfigSaved();
        openScreen(parent);
    }

    // ------------------------------------------------------------------ layout

    private CycleButton<Boolean> toggle(int index, String key, boolean value, java.util.function.Consumer<Boolean> setter) {
        return CycleButton.onOffBuilder(value)
                .create(columnX(index), rowY(index), OPTION_WIDTH, OPTION_HEIGHT,
                        Component.translatable(key), (button, newValue) -> setter.accept(newValue));
    }

    /** Options fill two columns, left to right then down. */
    private int columnX(int index) {
        return index % 2 == 0 ? this.width / 2 - 155 : this.width / 2 + 5;
    }

    private int rowY(int index) {
        return FIRST_ROW_Y + (index / 2) * ROW_STEP;
    }
}
