package me.flashyreese.mods.sodiumextra.client.util;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.util.Util;

public final class MacReducedResolution {
    /*
     * This branch only has the OpenGL presentation path. Request a non-Retina
     * drawable when creating the window, but do not assume the hint was applied:
     * NeoForge can reuse an early-loading window. Keep render dimensions no
     * larger than the logical window and scale presentation when necessary.
     */
    private static boolean openGlBackend;
    private static boolean enabledForWindow;

    public static boolean isEnabled() {
        return enabledForWindow;
    }

    public static int reduce(int value) {
        return Math.max(1, value / 2);
    }

    public static void useOpenGlBackend() {
        openGlBackend = true;
        // Snapshot this restart-required option when window hints are prepared.
        // Reading the live setting during presentation can disable upscaling
        // while the current window still uses a reduced render target.
        enabledForWindow = Util.getPlatform() == Util.OS.OSX
                && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac;
    }

    public static boolean shouldReduceFramebuffer() {
        return isEnabled() && !openGlBackend;
    }

    public static boolean shouldUseWindowSizeForFramebuffer() {
        return isEnabled() && openGlBackend;
    }

    public static int limitToWindowSize(int framebufferSize, int windowSize) {
        return Math.max(1, Math.min(framebufferSize, windowSize));
    }

    public static boolean shouldScalePresentation(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        // Fallback for any path where the render target and presentation target still disagree.
        return isEnabled() && (sourceWidth < targetWidth || sourceHeight < targetHeight);
    }
}
