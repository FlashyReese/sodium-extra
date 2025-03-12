package me.flashyreese.mods.sodiumextra.client.gui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.mixin.gui.MinecraftClientAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SodiumExtraHud implements HudRenderCallback, ClientTickEvents.StartTick {

    private final List<Text> textList = new ObjectArrayList<>();

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void onStartTick(MinecraftClient client) {
        // Clear the textList to start fresh (this might not be ideal but hey it's still better than whatever the fuck debug hud is doing)
        this.textList.clear();
        if (SodiumExtraClientMod.options().extraSettings.showFps) {
            int currentFPS = MinecraftClientAccessor.getCurrentFPS();

            Text text = Text.translatable("sodium-extra.overlay.fps", currentFPS);

            if (SodiumExtraClientMod.options().extraSettings.showFPSExtended)
                text = Text.literal(String.format("%s %s", text.getString(), Text.translatable("sodium-extra.overlay.fps_extended", SodiumExtraClientMod.getClientTickHandler().getHighestFps(), SodiumExtraClientMod.getClientTickHandler().getAverageFps(),
                        SodiumExtraClientMod.getClientTickHandler().getLowestFps()).getString()));

            this.textList.add(text);
        }

        if (SodiumExtraClientMod.options().extraSettings.showCoords && !this.client.hasReducedDebugInfo() && this.client.player != null) {
            Vec3d pos = this.client.player.getPos();

            Text text = Text.translatable("sodium-extra.overlay.coordinates", String.format("%.2f", pos.x), String.format("%.2f", pos.y), String.format("%.2f", pos.z));
            this.textList.add(text);
        }

        if (!SodiumExtraClientMod.options().renderSettings.lightUpdates) {
            Text text = Text.translatable("sodium-extra.overlay.light_updates");
            this.textList.add(text);
        }
    }
    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        if (!this.client.hasReducedDebugInfo() && !this.client.options.hudHidden) {
            SodiumExtraGameOptions.OverlayCorner overlayCorner = SodiumExtraClientMod.options().extraSettings.overlayCorner;
            // Calculate starting position based on the overlay corner
            int x;
            int y = overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT ?
                    this.client.getWindow().getScaledHeight() - this.client.textRenderer.fontHeight - 2 : 2;
            // Render each text in the list
            for (Text text : this.textList) {
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.TOP_RIGHT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    x = this.client.getWindow().getScaledWidth() - this.client.textRenderer.getWidth(text) - 2;
                } else {
                    x = 2;
                }
                this.drawString(drawContext, text, x, y);
                if (overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_LEFT || overlayCorner == SodiumExtraGameOptions.OverlayCorner.BOTTOM_RIGHT) {
                    y -= client.textRenderer.fontHeight + 2;
                } else {
                    y += client.textRenderer.fontHeight + 2; // Increase the y-position for the next text
                }
            }
        }
    }

    private void drawString(DrawContext drawContext, Text text, int x, int y) {
        int textColor = 0xffffffff; // Default text color

        if (SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.BACKGROUND) {
            drawContext.fill(x - 1, y - 1, x + this.client.textRenderer.getWidth(text) + 1, y + this.client.textRenderer.fontHeight + 1, -1873784752);
        }

        drawContext.drawText(this.client.textRenderer, text, x, y, textColor, SodiumExtraClientMod.options().extraSettings.textContrast == SodiumExtraGameOptions.TextContrast.SHADOW);
    }
}
