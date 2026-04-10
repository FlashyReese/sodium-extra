package me.flashyreese.mods.sodiumextra.client.hud;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudEditorScreen extends Screen {
    private static final int CONTEXT_MENU_PADDING = 4;
    private static final int CONTEXT_MENU_ROW_HEIGHT = 12;
    private final Screen previousScreen;
    private String selectedWidgetId;
    private int selectedOffsetX;
    private int selectedOffsetY;
    private String contextWidgetId;
    private int contextMenuX;
    private int contextMenuY;

    public HudEditorScreen(Screen previousScreen) {
        super(Component.translatable("sodium-extra.option.hud.editor.title"));
        this.previousScreen = previousScreen;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        HudManager hudManager = SodiumExtraClientMod.hudManager();
        hudManager.renderEditor(guiGraphics, this.selectedWidgetId);

        Component help = Component.translatable("sodium-extra.option.hud.editor.help");
        guiGraphics.text(this.font, help, (this.width - this.font.width(help)) / 2, 8, 0xFFFFFFFF, true);

        if (this.contextWidgetId != null) {
            this.drawContextMenu(guiGraphics, mouseX, mouseY);
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean dblClick) {
        if (mouseButtonEvent.button() == 1) {
            return this.handleContextClick(mouseButtonEvent);
        }

        if (mouseButtonEvent.button() != 0) {
            return super.mouseClicked(mouseButtonEvent, dblClick);
        }

        if (this.handleContextMenuSelection(mouseButtonEvent)) {
            return true;
        }

        HudWidget widget = SodiumExtraClientMod.hudManager().getWidgetByPoint(mouseButtonEvent.x(), mouseButtonEvent.y(), true);
        if (widget == null) {
            this.selectedWidgetId = null;
            this.setDragging(false);
            this.contextWidgetId = null;
            return super.mouseClicked(mouseButtonEvent, dblClick);
        }

        this.selectedWidgetId = widget.id();
        this.contextWidgetId = null;
        HudManager.WidgetBounds bounds = SodiumExtraClientMod.hudManager().getWidgetBounds(widget.id(), true);
        this.selectedOffsetX = (int) mouseButtonEvent.x() - bounds.x();
        this.selectedOffsetY = (int) mouseButtonEvent.y() - bounds.y();
        this.setDragging(true);
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY) {
        if (mouseButtonEvent.button() != 0 || this.selectedWidgetId == null) {
            return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
        }

        HudManager hudManager = SodiumExtraClientMod.hudManager();
        int drawX = (int) mouseButtonEvent.x() - this.selectedOffsetX;
        int drawY = (int) mouseButtonEvent.y() - this.selectedOffsetY;
        hudManager.moveWidget(this.selectedWidgetId, drawX, drawY, true);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() == 0 && this.selectedWidgetId != null) {
            SodiumExtraClientMod.hudManager().save();
            this.setDragging(false);
            return true;
        }

        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        SodiumExtraClientMod.hudManager().save();
        this.minecraft.setScreen(this.previousScreen);
    }

    private boolean handleContextClick(MouseButtonEvent mouseButtonEvent) {
        HudWidget widget = SodiumExtraClientMod.hudManager().getWidgetByPoint(mouseButtonEvent.x(), mouseButtonEvent.y(), true);
        if (widget == null) {
            this.contextWidgetId = null;
            return true;
        }

        this.selectedWidgetId = widget.id();
        this.contextWidgetId = widget.id();
        this.contextMenuX = (int) mouseButtonEvent.x();
        this.contextMenuY = (int) mouseButtonEvent.y();
        this.setDragging(false);
        return true;
    }

    private boolean handleContextMenuSelection(MouseButtonEvent mouseButtonEvent) {
        if (this.contextWidgetId == null) {
            return false;
        }

        int hovered = this.getContextMenuRow(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (hovered < 0) {
            this.contextWidgetId = null;
            return false;
        }

        HudManager hudManager = SodiumExtraClientMod.hudManager();
        switch (hovered) {
            case 0 -> hudManager.toggleWidgetEnabled(this.contextWidgetId);
            case 1 -> hudManager.cycleWidgetAnchor(this.contextWidgetId);
            case 2 -> hudManager.cycleWidgetBackground(this.contextWidgetId);
            case 3 -> hudManager.resetWidgetPosition(this.contextWidgetId);
            default -> {
                return false;
            }
        }

        hudManager.save();
        return true;
    }

    private void drawContextMenu(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        Component[] entries = this.getContextMenuEntries();
        int menuWidth = 0;
        for (Component entry : entries) {
            menuWidth = Math.max(menuWidth, this.font.width(entry));
        }

        int widthWithPadding = menuWidth + CONTEXT_MENU_PADDING * 2;
        int height = entries.length * CONTEXT_MENU_ROW_HEIGHT + CONTEXT_MENU_PADDING * 2;
        int x = Math.min(this.contextMenuX, this.width - widthWithPadding - 2);
        int y = Math.min(this.contextMenuY, this.height - height - 2);

        this.contextMenuX = x;
        this.contextMenuY = y;

        guiGraphics.fill(x, y, x + widthWithPadding, y + height, 0xC0101010);
        guiGraphics.fill(x, y, x + widthWithPadding, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y + height - 1, x + widthWithPadding, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x + widthWithPadding - 1, y, x + widthWithPadding, y + height, 0xFFFFFFFF);

        for (int i = 0; i < entries.length; i++) {
            int rowY = y + CONTEXT_MENU_PADDING + i * CONTEXT_MENU_ROW_HEIGHT;
            if (this.getContextMenuRow(mouseX, mouseY) == i) {
                guiGraphics.fill(x + 1, rowY - 1, x + widthWithPadding - 1, rowY + CONTEXT_MENU_ROW_HEIGHT - 1, 0x55FFFFFF);
            }
            guiGraphics.text(this.font, entries[i], x + CONTEXT_MENU_PADDING, rowY, 0xFFFFFFFF, false);
        }
    }

    private int getContextMenuRow(double mouseX, double mouseY) {
        Component[] entries = this.getContextMenuEntries();
        int menuWidth = 0;
        for (Component entry : entries) {
            menuWidth = Math.max(menuWidth, this.font.width(entry));
        }

        int widthWithPadding = menuWidth + CONTEXT_MENU_PADDING * 2;
        int height = entries.length * CONTEXT_MENU_ROW_HEIGHT + CONTEXT_MENU_PADDING * 2;

        if (mouseX < this.contextMenuX || mouseX > this.contextMenuX + widthWithPadding || mouseY < this.contextMenuY || mouseY > this.contextMenuY + height) {
            return -1;
        }

        int row = (int) ((mouseY - this.contextMenuY - CONTEXT_MENU_PADDING) / CONTEXT_MENU_ROW_HEIGHT);
        if (row < 0 || row >= entries.length) {
            return -1;
        }

        return row;
    }

    private Component[] getContextMenuEntries() {
        if (this.contextWidgetId == null) {
            return new Component[0];
        }

        HudManager manager = SodiumExtraClientMod.hudManager();
        if (manager.getWidget(this.contextWidgetId) == null) {
            return new Component[0];
        }

        var settings = manager.getWidgetSettingsView(this.contextWidgetId);
        return new Component[]{
                Component.translatable("sodium-extra.option.hud.context.enabled", settings.enabled ? Component.translatable("options.on") : Component.translatable("options.off")),
                Component.translatable("sodium-extra.option.hud.context.anchor", settings.anchor.getLocalizedName()),
                Component.translatable("sodium-extra.option.hud.context.background", settings.backgroundMode.getLocalizedName()),
                Component.translatable("sodium-extra.option.hud.context.reset_position")
        };
    }
}
