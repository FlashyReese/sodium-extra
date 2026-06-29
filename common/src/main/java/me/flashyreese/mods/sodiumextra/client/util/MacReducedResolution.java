package me.flashyreese.mods.sodiumextra.client.util;

import com.mojang.blaze3d.systems.GpuSurface;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.util.Util;

public final class MacReducedResolution {
    private static int windowWidth = -1;
    private static int windowHeight = -1;

    public static boolean isEnabled() {
        return Util.getPlatform() == Util.OS.OSX && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac;
    }

    public static int reduce(int value) {
        return Math.max(1, value / 2);
    }

    public static void rememberWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public static boolean shouldReduceFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return isEnabled() && isHighDpiFramebuffer(framebufferWidth, framebufferHeight, windowWidth, windowHeight);
    }

    public static GpuSurface.Configuration reduceSurfaceConfiguration(GpuSurface.Configuration config) {
        if (!shouldReduceFramebuffer(config.width(), config.height(), windowWidth, windowHeight)) {
            return config;
        }

        return new GpuSurface.Configuration(reduce(config.width()), reduce(config.height()), config.presentMode());
    }

    public static boolean shouldScalePresentation(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        return isEnabled() && (sourceWidth < targetWidth || sourceHeight < targetHeight);
    }

    private static boolean isHighDpiFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return windowWidth > 0 && windowHeight > 0 && (framebufferWidth > windowWidth || framebufferHeight > windowHeight);
    }
}
