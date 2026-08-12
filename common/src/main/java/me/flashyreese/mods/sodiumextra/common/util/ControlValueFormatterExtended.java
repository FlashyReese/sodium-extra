package me.flashyreese.mods.sodiumextra.common.util;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public interface ControlValueFormatterExtended extends ControlValueFormatter {
    static ControlValueFormatter resolution() {
        return (v) -> {
            Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
            if (monitor == null || monitor.modeCount() <= 0) {
                return Component.translatable("options.fullscreen.unavailable");
            } else {
                int modeIndex = Math.clamp(v - 1, 0, monitor.modeCount() - 1);
                if (v == 0) {
                    return Component.translatable("options.fullscreen.current");
                }
                VideoMode mode = monitor.mode(modeIndex);
                int w = mode.getWidth();
                int h = mode.getHeight();
                int g = gcd(w, h);
                String ratio = (w / g) + ":" + (h / g);
                String formatted = w + "x" + h + "@" + mode.getRefreshRate() + " (" + ratio + " | " + mode.getBitsPerPixel() + "bit)";
                return Component.literal(formatted);
            }
        };
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    static ControlValueFormatter fogDistance() {
        return (v) -> {
            if (v == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
                return Component.translatable("options.gamma.default");
            } else if (FogDistanceHelper.disablesFog(v)) {
                return Component.translatable("options.off");
            } else {
                return Component.translatable("options.chunks", v);
            }
        };
    }

    static ControlValueFormatter protectedFogDistance() {
        return (v) -> {
            if (v == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
                return Component.translatable("options.gamma.default");
            } else if (FogDistanceHelper.disablesFog(v)) {
                return Component.translatable("options.off");
            } else {
                return Component.translatable("sodium-extra.units.blocks", v);
            }
        };
    }

    static ControlValueFormatter ticks() {
        return (v) -> Component.translatable("sodium-extra.units.ticks", v);
    }
}
