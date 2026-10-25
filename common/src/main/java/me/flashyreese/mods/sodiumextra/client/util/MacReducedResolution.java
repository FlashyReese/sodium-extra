package me.flashyreese.mods.sodiumextra.client.util;

import com.mojang.blaze3d.systems.GpuSurface;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.util.Util;

public final class MacReducedResolution {
    /*
     * This is intentionally backend-specific. On macOS, the "reduce resolution"
     * option cannot be treated as one generic framebuffer rule:
     *
     * - OpenGL requests GLFW_COCOA_RETINA_FRAMEBUFFER=false, but NeoForge can
     *   reuse an early-loading window. Limit rendering to the logical window
     *   size without halving an already reduced drawable.
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
    private static boolean enabledForWindow;
    private static int windowWidth = -1;
    private static int windowHeight = -1;

    public static boolean isEnabled() {
        return enabledForWindow;
    }

    public static int reduce(int value) {
        return Math.max(1, value / 2);
    }

    public static void useOpenGlBackend() {
        useBackend(Backend.OPENGL);
    }

    public static void useVulkanBackend() {
        useBackend(Backend.VULKAN);
    }

    private static void useBackend(Backend selectedBackend) {
        backend = selectedBackend;
        // Snapshot this restart-required option when window hints are prepared.
        // Reading the live setting during presentation can disable upscaling
        // while the current window still uses a reduced render target.
        enabledForWindow = Util.getPlatform() == Util.OS.OSX
                && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac;
        windowWidth = -1;
        windowHeight = -1;
    }

    public static void rememberWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public static boolean shouldReduceFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return isEnabled() && backend != Backend.OPENGL && isHighDpiFramebuffer(framebufferWidth, framebufferHeight, windowWidth, windowHeight);
    }

    public static boolean shouldUseWindowSizeForFramebuffer() {
        return isEnabled() && backend == Backend.OPENGL;
    }

    public static GpuSurface.Configuration reduceSurfaceConfiguration(GpuSurface.Configuration config) {
        if (!isEnabled() || backend != Backend.VULKAN || !isHighDpiFramebuffer(config.width(), config.height(), windowWidth, windowHeight)) {
            return config;
        }

        return new GpuSurface.Configuration(reduce(config.width()), reduce(config.height()), config.presentMode());
    }

    public static int limitToWindowSize(int framebufferSize, int windowSize) {
        return Math.max(1, Math.min(framebufferSize, windowSize));
    }

    public static boolean shouldScalePresentation(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        // Fallback for any path where the render target and presentation target still disagree.
        return isEnabled() && (sourceWidth < targetWidth || sourceHeight < targetHeight);
    }

    private static boolean isHighDpiFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return windowWidth > 0 && windowHeight > 0 && (framebufferWidth > windowWidth || framebufferHeight > windowHeight);
    }
}
