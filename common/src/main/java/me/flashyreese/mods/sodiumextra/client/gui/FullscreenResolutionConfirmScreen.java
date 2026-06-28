package me.flashyreese.mods.sodiumextra.client.gui;

import com.mojang.blaze3d.platform.VideoMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class FullscreenResolutionConfirmScreen extends ConfirmScreen {
    private static final int TIMEOUT_TICKS = 15 * 20;

    private int ticksRemaining = TIMEOUT_TICKS;

    public FullscreenResolutionConfirmScreen(Optional<VideoMode> previousMode) {
        super(
                accepted -> {
                    if (accepted) {
                        FullscreenResolutionConfirmation.keep();
                    } else {
                        FullscreenResolutionConfirmation.revert(previousMode);
                    }
                    Minecraft.getInstance().setScreen(null);
                },
                Component.translatable("sodium-extra.option.wayland_fullscreen_resolution.confirm.title"),
                Component.translatable("sodium-extra.option.wayland_fullscreen_resolution.confirm.message"),
                Component.translatable("sodium-extra.option.wayland_fullscreen_resolution.confirm.keep"),
                Component.translatable("sodium-extra.option.wayland_fullscreen_resolution.confirm.revert")
        );
    }

    @Override
    public void tick() {
        super.tick();

        this.ticksRemaining--;
        if (this.ticksRemaining <= 0) {
            this.callback.accept(false);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
