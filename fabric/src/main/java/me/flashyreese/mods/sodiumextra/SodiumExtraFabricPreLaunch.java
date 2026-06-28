package me.flashyreese.mods.sodiumextra;

import me.flashyreese.mods.sodiumextra.client.recovery.WaylandFullscreenResolutionRecovery;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class SodiumExtraFabricPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        FabricLoader loader = FabricLoader.getInstance();
        WaylandFullscreenResolutionRecovery.recoverIfNeeded(loader.getGameDir(), loader.getConfigDir());
    }
}
