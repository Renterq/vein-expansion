package dev.renterq.veinexpansion.gui;

import dev.renterq.veinexpansion.config.VeinExpansionConfig;
import dev.renterq.veinexpansion.platform.PlatformHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Searchable, paged list of toggles in the same layout as DuraHUD's effect picker: two columns,
 * twelve entries a page, and a {@code < Done >} row at the bottom.
 */
abstract class PagedPickerScreen extends VeinScreen {
    /** One toggle in the list. */
    protected record Entry(Component label, String tooltip, boolean selected, Runnable toggle) {}

    private static final int OPTION_WIDTH = 150;
    private static final int OPTION_HEIGHT = 20;
    private static final int FIRST_ROW_Y = 52;
    private static final int ROW_STEP = 22;
    private static final int PER_PAGE = 12;

    private final Screen parent;
    private final List<Button> rowButtons = new ArrayList<>();
    private EditBox search;
    private Button previous;
    private Button next;
    private String query = "";
    private int page;
    private int pages = 1;

    protected PagedPickerScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    /** Entries matching the (lower-case) search text. */
    protected abstract List<Entry> entries(String query);

    protected abstract Component counter();

    @Override
    protected void init() {
        search = new EditBox(this.font, this.width / 2 - 100, 26, 200, 18, Component.translatable("vein_expansion.picker.search"));
        search.setHint(Component.translatable("vein_expansion.picker.search"));
        search.setValue(query);
        search.setResponder(value -> {
            query = value.toLowerCase();
            page = 0;
            refresh();
        });
        addRenderableWidget(search);

        previous = Button.builder(Component.literal("<"), ignored -> {
            page--;
            refresh();
        }).bounds(this.width / 2 - 155, this.height - 28, 74, OPTION_HEIGHT).build();
        addRenderableWidget(previous);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> onClose())
                .bounds(this.width / 2 - 37, this.height - 28, 74, OPTION_HEIGHT).build());

        next = Button.builder(Component.literal(">"), ignored -> {
            page++;
            refresh();
        }).bounds(this.width / 2 + 81, this.height - 28, 74, OPTION_HEIGHT).build();
        addRenderableWidget(next);

        rowButtons.clear();
        refresh();
    }

    private void refresh() {
        for (Button button : rowButtons) {
            removeWidget(button);
        }
        rowButtons.clear();

        List<Entry> entries = entries(query);
        pages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));

        int start = page * PER_PAGE;
        int end = Math.min(entries.size(), start + PER_PAGE);
        for (int i = start; i < end; i++) {
            Entry entry = entries.get(i);
            int index = i - start;
            Component label = entry.selected()
                    ? Component.literal("✔ ").append(entry.label())
                    : entry.label();
            Button button = Button.builder(label, ignored -> {
                entry.toggle().run();
                refresh();
            }).bounds(columnX(index), rowY(index), OPTION_WIDTH, OPTION_HEIGHT).build();
            button.setTooltip(Tooltip.create(Component.literal(entry.tooltip())));
            rowButtons.add(button);
            addRenderableWidget(button);
        }

        previous.active = page > 0;
        next.active = page < pages - 1;
    }

    @Override
    protected void drawOverlay(Gfx gfx, int mouseX, int mouseY, float delta) {
        gfx.centered(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        gfx.centered(this.font, counter().copy().append("  (" + (page + 1) + "/" + pages + ")"),
                this.width / 2, 46, 0xFFA0A0A0);
    }

    @Override
    public void onClose() {
        VeinExpansionConfig.get().save();
        PlatformHelper.onConfigSaved();
        openScreen(parent);
    }

    private int columnX(int index) {
        return index % 2 == 0 ? this.width / 2 - 155 : this.width / 2 + 5;
    }

    private int rowY(int index) {
        return FIRST_ROW_Y + 8 + (index / 2) * ROW_STEP;
    }
}
