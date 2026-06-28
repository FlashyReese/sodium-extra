package me.flashyreese.mods.sodiumextra.client.util;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.Util;

public final class MacReducedResolution {
    public static boolean isEnabled() {
        return Util.getPlatform() == Util.OS.OSX && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac;
    }

    public static int reduce(int value) {
        return Math.max(1, value / 2);
    }

    public static boolean shouldScalePresentation(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        return isEnabled() && (sourceWidth < targetWidth || sourceHeight < targetHeight);
    }
}
