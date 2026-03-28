package me.flashyreese.mods.sodiumextra.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SodiumExtraDebugEntryCoords implements DebugScreenEntry {
    @Override
    public void display(@NonNull DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.getCameraEntity();
        if (entity != null) {
            Vec3 pos = minecraft.getCameraEntity().position();
            Component text = Component.translatable("sodium-extra.overlay.coordinates", String.format("%.2f", pos.x), String.format("%.2f", pos.y), String.format("%.2f", pos.z));
            displayer.addLine(text.getString());
        }
    }

    @Override
    public @NonNull DebugEntryCategory category() {
        return SodiumExtraDebugEntryCategory.SODIUM_EXTRA_DEBUG_CATEGORY;
    }
}
