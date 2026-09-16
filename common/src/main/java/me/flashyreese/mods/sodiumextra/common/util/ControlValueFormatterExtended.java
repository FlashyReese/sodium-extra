package me.flashyreese.mods.sodiumextra.common.util;

import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.minecraft.network.chat.Component;

public interface ControlValueFormatterExtended extends ControlValueFormatter {
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
