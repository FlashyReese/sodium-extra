package me.flashyreese.mods.sodiumextra.client.gui;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SodiumExtraDebugEntryLightUpdates implements DebugScreenEntry {
    @Override
    public void display(@NonNull DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        if (!SodiumExtraClientMod.options().renderSettings.lightUpdates) {
            Component text = Component.translatable("sodium-extra.overlay.light_updates");
            displayer.addPriorityLine(text.getString());
        }
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return SodiumExtraClientMod.SODIUM_EXTRA_DEBUG_CATEGORY;
    }
}
