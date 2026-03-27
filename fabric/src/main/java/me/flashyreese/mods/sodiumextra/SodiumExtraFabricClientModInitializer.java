package me.flashyreese.mods.sodiumextra;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.fabricmc.api.ClientModInitializer;

public class SodiumExtraFabricClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SodiumExtraClientMod.init();
    }
}
