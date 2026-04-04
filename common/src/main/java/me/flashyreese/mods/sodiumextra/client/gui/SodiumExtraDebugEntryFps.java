package me.flashyreese.mods.sodiumextra.client.gui;

import me.flashyreese.mods.sodiumextra.client.FrameCounter;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SodiumExtraDebugEntryFps implements DebugScreenEntry {
    private final FrameCounter stats = FrameCounter.getInstance();

    private final boolean extended;

    public SodiumExtraDebugEntryFps(boolean extended) {
        this.extended = extended;
    }

    @Override
    public void display(@NonNull DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        int currentFPS = FrameCounter.getInstance().getSmoothFps();
        Component text = Component.translatable("sodium-extra.overlay.fps", currentFPS);

        if (this.extended)
            text = Component.literal(String.format("%s %s", text.getString(), Component.translatable("sodium-extra.overlay.fps_extended", this.stats.getAverageFps(), this.stats.getOnePercentLowFps(),
                    this.stats.getPointOnePercentLowFps()).getString()));
        displayer.addLine(text.getString());
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return SodiumExtraDebugEntryCategory.SODIUM_EXTRA_DEBUG_CATEGORY;
    }
}
