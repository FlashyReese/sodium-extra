package me.flashyreese.mods.sodiumextra.client.hud;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class HudManager {
    private static final int SCREEN_MARGIN = 2;

    private final Minecraft client = Minecraft.getInstance();
    private final Map<String, HudWidget> widgets = new Object2ObjectArrayMap<>();

    public HudManager() {
        this.widgets.put("fps", new FpsHudWidget());
        this.widgets.put("coordinates", new CoordinatesHudWidget());
    }

    public void tick() {
        for (HudWidget widget : this.widgets.values()) {
            widget.tick();
        }
    }

    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!this.canRenderInGameHud()) {
            return;
        }

        this.renderWidgets(guiGraphics, false, null);
    }

    public void renderEditor(GuiGraphicsExtractor guiGraphics, String selectedWidgetId) {
        this.renderWidgets(guiGraphics, true, selectedWidgetId);
    }

    public boolean canRenderInGameHud() {
        if (this.client.options.hideGui || this.client.debugEntries.isOverlayVisible()) {
            return false;
        }

        Screen screen = this.client.screen;
        return screen == null || screen instanceof HudEditorScreen;
    }

    public HudWidget getWidgetByPoint(double mouseX, double mouseY) {
        return this.getWidgetByPoint(mouseX, mouseY, false);
    }

    public HudWidget getWidgetByPoint(double mouseX, double mouseY, boolean includeDisabled) {
        for (HudWidget widget : this.widgets.values()) {
            SodiumExtraGameOptions.HudWidgetSettings widgetSettings = this.getWidgetSettings(widget.id());
            if (!includeDisabled && !widgetSettings.enabled) {
                continue;
            }

            WidgetBounds bounds = this.getWidgetBounds(widget.id(), includeDisabled);
            int widgetWidth = bounds.width();
            int drawX = bounds.x();
            int drawY = bounds.y();

            if (mouseX >= drawX && mouseX <= drawX + widgetWidth && mouseY >= drawY && mouseY <= drawY + widget.getHeight()) {
                return widget;
            }
        }

        return null;
    }

    public HudWidget getWidget(String widgetId) {
        return this.widgets.get(widgetId);
    }

    public WidgetBounds getWidgetBounds(String widgetId, boolean editorMode) {
        HudWidget widget = this.widgets.get(widgetId);
        if (widget == null) {
            return new WidgetBounds(0, 0, 0, 0);
        }

        int screenWidth = this.client.getWindow().getGuiScaledWidth();
        int screenHeight = this.client.getWindow().getGuiScaledHeight();
        int widgetWidth = this.getWidgetWidth(widget, editorMode);
        int widgetHeight = widget.getHeight();
        SodiumExtraGameOptions.HudWidgetSettings settings = this.getWidgetSettings(widgetId);

        int drawX = this.getDrawX(settings, widgetWidth, screenWidth);
        int drawY = this.resolveY(settings.y, widgetHeight, screenHeight);

        int clampedX = this.clamp(drawX, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - widgetWidth - SCREEN_MARGIN));
        int clampedY = this.clamp(drawY, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenHeight - widgetHeight - SCREEN_MARGIN));

        return new WidgetBounds(clampedX, clampedY, widgetWidth, widgetHeight);
    }

    public void moveWidget(String widgetId, int drawX, int drawY) {
        this.moveWidget(widgetId, drawX, drawY, false);
    }

    public void moveWidget(String widgetId, int drawX, int drawY, boolean editorMode) {
        HudWidget widget = this.widgets.get(widgetId);
        if (widget == null) {
            return;
        }

        SodiumExtraGameOptions.HudWidgetSettings settings = this.getWidgetSettings(widgetId);
        int screenWidth = this.client.getWindow().getGuiScaledWidth();
        int screenHeight = this.client.getWindow().getGuiScaledHeight();
        int widgetWidth = this.getWidgetWidth(widget, editorMode);

        int clampedY = this.clamp(drawY, SCREEN_MARGIN, screenHeight - widget.getHeight() - SCREEN_MARGIN);
        settings.y = clampedY;

        int clampedX = this.clamp(drawX, SCREEN_MARGIN, screenWidth - widgetWidth - SCREEN_MARGIN);
        if (settings.anchor == SodiumExtraGameOptions.HudHorizontalAnchor.LEFT) {
            settings.x = clampedX;
        } else {
            settings.x = clampedX + widgetWidth - screenWidth;
        }
    }

    public void save() {
        SodiumExtraClientMod.options().writeChanges();
    }

    public SodiumExtraGameOptions.HudWidgetSettings getWidgetSettingsView(String widgetId) {
        return this.getWidgetSettings(widgetId);
    }

    public void toggleWidgetEnabled(String widgetId) {
        SodiumExtraGameOptions.HudWidgetSettings settings = this.getWidgetSettings(widgetId);
        settings.enabled = !settings.enabled;
    }

    public void cycleWidgetAnchor(String widgetId) {
        SodiumExtraGameOptions.HudWidgetSettings settings = this.getWidgetSettings(widgetId);
        settings.anchor = settings.anchor == SodiumExtraGameOptions.HudHorizontalAnchor.LEFT
                ? SodiumExtraGameOptions.HudHorizontalAnchor.RIGHT
                : SodiumExtraGameOptions.HudHorizontalAnchor.LEFT;
    }

    public void cycleWidgetBackground(String widgetId) {
        SodiumExtraGameOptions.HudWidgetSettings settings = this.getWidgetSettings(widgetId);
        settings.backgroundMode = switch (settings.backgroundMode) {
            case NONE -> SodiumExtraGameOptions.HudBackgroundMode.SHADOW;
            case SHADOW -> SodiumExtraGameOptions.HudBackgroundMode.BACKGROUND;
            case BACKGROUND -> SodiumExtraGameOptions.HudBackgroundMode.NONE;
        };
    }

    public void resetWidgetPosition(String widgetId) {
        SodiumExtraGameOptions.HudWidgetSettings settings = this.getWidgetSettings(widgetId);
        switch (widgetId) {
            case "fps" -> {
                settings.x = settings.anchor == SodiumExtraGameOptions.HudHorizontalAnchor.RIGHT ? -2 : 2;
                settings.y = 2;
            }
            case "coordinates" -> {
                settings.x = settings.anchor == SodiumExtraGameOptions.HudHorizontalAnchor.RIGHT ? -2 : 2;
                settings.y = 14;
            }
        }
    }

    private void renderWidgets(GuiGraphicsExtractor guiGraphics, boolean editorMode, String selectedWidgetId) {
        int screenWidth = this.client.getWindow().getGuiScaledWidth();
        int screenHeight = this.client.getWindow().getGuiScaledHeight();

        for (HudWidget widget : this.widgets.values()) {
            SodiumExtraGameOptions.HudWidgetSettings settings = this.getWidgetSettings(widget.id());
            if (!settings.enabled && !editorMode) {
                continue;
            }

            int textWidth = this.getWidgetWidth(widget, editorMode);
            int textHeight = widget.getHeight();
            int drawX = this.getDrawX(settings, textWidth, screenWidth);
            int drawY = this.resolveY(settings.y, textHeight, screenHeight);

            int clampedDrawX = this.clamp(drawX, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - textWidth - SCREEN_MARGIN));
            int clampedDrawY = this.clamp(drawY, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenHeight - textHeight - SCREEN_MARGIN));

            if (clampedDrawX != drawX || clampedDrawY != drawY) {
                this.moveWidget(widget.id(), clampedDrawX, clampedDrawY, editorMode);
            }

            this.drawWidgetText(guiGraphics, widget, settings, clampedDrawX, clampedDrawY, editorMode);

            if (editorMode) {
                int borderColor = widget.id().equals(selectedWidgetId) ? 0xFFFFD454 : 0xB0FFFFFF;
                int boxLeft = clampedDrawX - 2;
                int boxTop = clampedDrawY - 2;
                int boxRight = clampedDrawX + textWidth + 3;
                int boxBottom = clampedDrawY + textHeight + 3;

                guiGraphics.fill(boxLeft, boxTop, boxRight, boxBottom, 0x55000000);
                this.drawBorder(guiGraphics, boxLeft, boxTop, boxRight, boxBottom, borderColor);
                Component indicator = settings.anchor == SodiumExtraGameOptions.HudHorizontalAnchor.LEFT ? Component.literal("L") : Component.literal("R");
                guiGraphics.text(this.client.font, indicator, clampedDrawX + textWidth + 4, clampedDrawY, 0xFFAAAAAA, true);
            }
        }
    }

    private void drawWidgetText(GuiGraphicsExtractor guiGraphics, HudWidget widget, SodiumExtraGameOptions.HudWidgetSettings settings, int x, int y, boolean editorMode) {
        Component text = editorMode && !settings.enabled ? Component.translatable("sodium-extra.option.hud.widget.disabled", widget.name()) : widget.getText();
        int textWidth = this.client.font.width(text);
        int textHeight = widget.getHeight();

        if (settings.backgroundMode == SodiumExtraGameOptions.HudBackgroundMode.BACKGROUND) {
            guiGraphics.fill(x - 1, y - 1, x + textWidth + 1, y + textHeight + 1, -1873784752);
        }

        boolean shadow = settings.backgroundMode == SodiumExtraGameOptions.HudBackgroundMode.SHADOW || editorMode;
        guiGraphics.text(this.client.font, text, x, y, 0xFFFFFFFF, shadow);
    }

    private int resolveY(int rawY, int widgetHeight, int screenHeight) {
        if (rawY < 0) {
            return screenHeight + rawY;
        }

        return rawY;
    }

    private int getDrawX(SodiumExtraGameOptions.HudWidgetSettings settings, int textWidth, int screenWidth) {
        if (settings.anchor == SodiumExtraGameOptions.HudHorizontalAnchor.RIGHT) {
            int base = settings.x < 0 ? screenWidth + settings.x : settings.x;
            return base - textWidth;
        }

        return settings.x;
    }

    private SodiumExtraGameOptions.HudWidgetSettings getWidgetSettings(String widgetId) {
        SodiumExtraGameOptions.ExtraSettings extraSettings = SodiumExtraClientMod.options().extraSettings;
        return switch (widgetId) {
            case "fps" -> extraSettings.fpsWidget;
            case "coordinates" -> extraSettings.coordinatesWidget;
            default -> throw new IllegalArgumentException("Unknown widget id: " + widgetId);
        };
    }

    private int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }

        return Math.max(min, Math.min(max, value));
    }

    private int getWidgetWidth(HudWidget widget, boolean editorMode) {
        if (editorMode) {
            return Math.max(widget.getWidth(), this.client.font.width(widget.name()));
        }

        return widget.getWidth();
    }

    private void drawBorder(GuiGraphicsExtractor guiGraphics, int left, int top, int right, int bottom, int color) {
        guiGraphics.fill(left, top, right, top + 1, color);
        guiGraphics.fill(left, bottom - 1, right, bottom, color);
        guiGraphics.fill(left, top + 1, left + 1, bottom - 1, color);
        guiGraphics.fill(right - 1, top + 1, right, bottom - 1, color);
    }

    public record WidgetBounds(int x, int y, int width, int height) {
    }
}
