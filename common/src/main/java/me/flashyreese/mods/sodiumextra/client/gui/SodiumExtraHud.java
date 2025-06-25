package me.flashyreese.mods.sodiumextra.client.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.sodiumextra.client.FrameCounter;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SodiumExtraHud {

    private final List<Component> textList = new ObjectArrayList<>();

    private final Minecraft client = Minecraft.getInstance();

    private final FrameCounter stats = FrameCounter.getInstance();

    public void onStartTick(Minecraft client) {
        // Clear the textList to start fresh (this might not be ideal but hey it's still better than whatever the fuck debug hud is doing)
        this.textList.clear();
        if (SodiumExtraClientMod.options().extraSettings.showFps) {
            int currentFPS = FrameCounter.getInstance().getSmoothFps();

            Component text = Component.translatable("sodium-extra.overlay.fps", currentFPS);

            if (SodiumExtraClientMod.options().extraSettings.showFPSExtended)
                text = Component.literal(String.format("%s %s", text.getString(), Component.translatable("sodium-extra.overlay.fps_extended", this.stats.getAverageFps(), this.stats.getOnePercentLowFps(),
                        this.stats.getPointOnePercentLowFps()).getString()));

            this.textList.add(text);
        }

        if (SodiumExtraClientMod.options().extraSettings.showCoords && this.client.player != null) {
            Vec3 pos = this.client.player.position();

            Component text = Component.translatable("sodium-extra.overlay.coordinates", String.format("%.2f", pos.x), String.format("%.2f", pos.y), String.format("%.2f", pos.z));
            if (this.client.showOnlyReducedInfo()) {
                text = Component.literal("Cords not available due to reducedDebugInfo: true."); // Todo: Localize?
            }

            this.textList.add(text);
        }

        if (!SodiumExtraClientMod.options().renderSettings.lightUpdates) {
            Component text = Component.translatable("sodium-extra.overlay.light_updates");
            this.textList.add(text);
        }
    }

    public void onHudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!this.client.getDebugOverlay().showDebugScreen() && !this.client.options.hideGui) {
            SodiumExtraGameOptions.OverlayCorner overlayCorner = SodiumExtraClientMod.options().extraSettings.overlayCorner;
            // Calculate starting position based on the overlay corner
            int x;
            int y = overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT ?
                    this.client.getWindow().getGuiScaledHeight() - this.client.font.lineHeight - 2 : 2;
            // Render each text in the list
            for (Component text : this.textList) {
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.TOP_RIGHT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    x = this.client.getWindow().getGuiScaledWidth() - this.client.font.width(text) - 2;
                } else {
                    x = 2;
                }
                this.drawString(guiGraphics, text, x, y);
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    y -= client.font.lineHeight + 2;
                } else {
                    y += client.font.lineHeight + 2; // Increase the y-position for the next text
                }
            }
        }
    }

    private void drawString(GuiGraphics guiGraphics, Component text, int x, int y) {
        int textColor = 0xffffffff; // Default text color

        if (SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.BACKGROUND) {
            guiGraphics.fill(x - 1, y - 1, x + this.client.font.width(text) + 1, y + this.client.font.lineHeight + 1, -1873784752);
        }

        guiGraphics.drawString(this.client.font, text, x, y, textColor, SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.SHADOW);
    }
}
