package me.flashyreese.mods.sodiumextra;

import me.flashyreese.mods.sodiumextra.client.recovery.WaylandFullscreenResolutionRecovery;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(value = "sodium_extra", dist = Dist.CLIENT)
public class SodiumExtraNeoForgeClientMod {
    public SodiumExtraNeoForgeClientMod(IEventBus bus, ModContainer modContainer) {
        WaylandFullscreenResolutionRecovery.recoverIfNeeded(FMLPaths.GAMEDIR.get(), FMLPaths.CONFIGDIR.get());
    }
}
