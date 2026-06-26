package me.flashyreese.mods.sodiumextra.client.gui;

import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Minecraft;

import java.util.Optional;

/**
 * Schedules the post-switch confirmation prompt for Wayland/XWayland fullscreen mode changes.
 * The prompt handles bad modes that return; pre-launch recovery handles hangs before it can open.
 */
public final class FullscreenResolutionConfirmation {
    private static boolean requested;
    private static Optional<VideoMode> previousMode = Optional.empty();

    public static void request(Optional<VideoMode> previousVideoMode) {
        requested = true;
        previousMode = previousVideoMode;
    }

    public static void tick(Minecraft client) {
        if (!requested || client.getWindow() == null) {
            return;
        }

        requested = false;
        Optional<VideoMode> previous = previousMode;
        previousMode = Optional.empty();
        client.setScreenAndShow(new FullscreenResolutionConfirmScreen(previous));
    }

    static void keep() {
        SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
    }

    static void revert(Optional<VideoMode> previousVideoMode) {
        Window window = Minecraft.getInstance().getWindow();
        window.setPreferredFullscreenVideoMode(previousVideoMode);
        window.changeFullscreenVideoMode();
        Minecraft.getInstance().options.save();
        SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
    }
}
