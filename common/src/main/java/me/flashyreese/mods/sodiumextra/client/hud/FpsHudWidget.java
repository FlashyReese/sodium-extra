package me.flashyreese.mods.sodiumextra.client.hud;

import me.flashyreese.mods.sodiumextra.client.FrameCounter;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.network.chat.Component;

public class FpsHudWidget extends AbstractTextHudWidget {
    private final FrameCounter stats = FrameCounter.getInstance();

    public FpsHudWidget() {
        super("fps", Component.translatable("sodium-extra.option.show_fps"));
    }

    @Override
    protected Component getTextInternal() {
        int currentFPS = this.stats.getSmoothFps();
        Component text = Component.translatable("sodium-extra.overlay.fps", currentFPS);

        if (SodiumExtraClientMod.options().extraSettings.showFPSExtended) {
            text = Component.literal(text.getString() + " " + Component.translatable("sodium-extra.overlay.fps_extended", this.stats.getAverageFps(), this.stats.getOnePercentLowFps(), this.stats.getPointOnePercentLowFps()).getString());
        }

        return text;
    }
}
