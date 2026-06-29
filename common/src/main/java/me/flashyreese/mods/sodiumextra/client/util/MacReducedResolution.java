package me.flashyreese.mods.sodiumextra.client.util;

import com.mojang.blaze3d.systems.GpuSurface;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.util.Util;

public final class MacReducedResolution {
    /*
     * This is intentionally backend-specific. On macOS, the "reduce resolution"
     * option cannot be treated as one generic framebuffer rule:
     *
     * - OpenGL honors GLFW_COCOA_RETINA_FRAMEBUFFER=false and already gives us a
     *   logical-size drawable. Halving Window.framebufferWidth/Height again makes
     *   a 1440p Retina setup render as 720p.
     * - Vulkan/MoltenVK still needs Minecraft's Window framebuffer dimensions to
     *   be reduced, and its swapchain should be reduced to match. Rendering into a
     *   reduced texture and scaling it into a full-size swapchain fixed the border
     *   but caused bad 1% lows on the Mac tester's machine.
     */
    private enum Backend {
        UNKNOWN,
        OPENGL,
        VULKAN
    }

    private static Backend backend = Backend.UNKNOWN;
    private static int windowWidth = -1;
    private static int windowHeight = -1;

    public static boolean isEnabled() {
        return Util.getPlatform() == Util.OS.OSX && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac;
    }

    public static int reduce(int value) {
        return Math.max(1, value / 2);
    }

    public static void useOpenGlBackend() {
        backend = Backend.OPENGL;
    }

    public static void useVulkanBackend() {
        backend = Backend.VULKAN;
    }

    public static void rememberWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public static boolean shouldReduceFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return isEnabled() && backend != Backend.OPENGL && isHighDpiFramebuffer(framebufferWidth, framebufferHeight, windowWidth, windowHeight);
    }

    public static boolean shouldUseWindowSizeForInitialFramebuffer() {
        return isEnabled() && backend == Backend.OPENGL;
    }

    public static GpuSurface.Configuration reduceSurfaceConfiguration(GpuSurface.Configuration config) {
        if (!isEnabled() || backend != Backend.VULKAN || !isHighDpiFramebuffer(config.width(), config.height(), windowWidth, windowHeight)) {
            return config;
        }

        return new GpuSurface.Configuration(reduce(config.width()), reduce(config.height()), config.presentMode());
    }

    public static boolean shouldScalePresentation(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        // Fallback for any path where the render target and presentation target still disagree.
        return isEnabled() && (sourceWidth < targetWidth || sourceHeight < targetHeight);
    }

    private static boolean isHighDpiFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return windowWidth > 0 && windowHeight > 0 && (framebufferWidth > windowWidth || framebufferHeight > windowHeight);
    }
}
