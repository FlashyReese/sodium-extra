package me.flashyreese.mods.sodiumextra.common.util;

import com.mojang.blaze3d.platform.Monitor;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public interface ControlValueFormatterExtended extends ControlValueFormatter {
    static ControlValueFormatter resolution() {
        return (v) -> {
            Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
            if (monitor == null || monitor.getModeCount() <= 0) {
                return Component.translatable("options.fullscreen.unavailable");
            } else {
                int modeIndex = Math.clamp(v - 1, 0, monitor.getModeCount() - 1);
                return v == 0 ? Component.translatable("options.fullscreen.current") : Component.literal(monitor.getMode(modeIndex).toString().replace(" (24bit)", ""));
            }
        };
    }

    static ControlValueFormatter ticks() {
        return (v) -> Component.translatable("sodium-extra.units.ticks", v);
    }
}
