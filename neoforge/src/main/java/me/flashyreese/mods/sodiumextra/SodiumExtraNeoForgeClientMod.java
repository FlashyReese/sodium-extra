package me.flashyreese.mods.sodiumextra;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = "sodium_extra", dist = Dist.CLIENT)
public class SodiumExtraNeoForgeClientMod {
    public SodiumExtraNeoForgeClientMod(IEventBus bus, ModContainer modContainer) {
        SodiumExtraClientMod.init();
    }
}