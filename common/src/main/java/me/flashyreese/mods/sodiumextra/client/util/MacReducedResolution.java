package me.flashyreese.mods.sodiumextra.client.util;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.Util;

public final class MacReducedResolution {
    /*
     * This branch only has the OpenGL presentation path. On macOS, the reduced
     * resolution option is handled by GLFW_COCOA_RETINA_FRAMEBUFFER=false, which
     * gives Minecraft a logical-size drawable. Halving Window.framebufferWidth
     * and Window.framebufferHeight after that makes a 1440p Retina setup render
     * as 720p, so Window's framebuffer fields must not be reduced again.
     */
    private static boolean openGlBackend;

    public static boolean isEnabled() {
        return Util.getPlatform() == Util.OS.OSX && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac;
    }

    public static int reduce(int value) {
        return Math.max(1, value / 2);
    }

    public static void useOpenGlBackend() {
        openGlBackend = true;
    }

    public static boolean shouldReduceFramebuffer() {
        return isEnabled() && !openGlBackend;
    }

    public static boolean shouldUseWindowSizeForInitialFramebuffer() {
        return isEnabled() && openGlBackend;
    }

    public static boolean shouldScalePresentation(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        // Fallback for any path where the render target and presentation target still disagree.
        return isEnabled() && (sourceWidth < targetWidth || sourceHeight < targetHeight);
    }
}
