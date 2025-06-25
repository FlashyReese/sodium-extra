package me.flashyreese.mods.sodiumextra.common.util;

import com.mojang.blaze3d.platform.Monitor;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public interface ControlValueFormatterExtended extends ControlValueFormatter {
    static ControlValueFormatter resolution() {
        Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
        return (v) -> {
            if (monitor == null) {
                return Component.translatable("options.fullscreen.unavailable");
            } else {
                return v == 0 ? Component.translatable("options.fullscreen.current") : Component.literal(monitor.getMode(v - 1).toString());
            }
        };
    }

    static ControlValueFormatter ticks() {
        return (v) -> Component.translatable("sodium-extra.units.ticks", v);
    }
}
