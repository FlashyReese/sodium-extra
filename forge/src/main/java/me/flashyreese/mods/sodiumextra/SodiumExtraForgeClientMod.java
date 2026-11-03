package me.flashyreese.mods.sodiumextra;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.recovery.WaylandFullscreenResolutionRecovery;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

// Forge's @Mod takes no dist; client-only comes from clientSideOnly in mods.toml.
@Mod("sodium_extra")
public class SodiumExtraForgeClientMod {
    public SodiumExtraForgeClientMod(FMLJavaModLoadingContext context) {
        WaylandFullscreenResolutionRecovery.recoverIfNeeded(FMLPaths.GAMEDIR.get(), FMLPaths.CONFIGDIR.get());

        // NeoForge funnels this through RegisterDebugEntriesEvent, which Forge has no equivalent for.
        // Vanilla's DebugScreenEntries.register is public though, so the entries can be added
        // directly and no event is needed.
        SodiumExtraClientMod.registerAll(DebugScreenEntries::register);

        // The NeoForge build additionally calls event.includeInProfile(...) to pre-list the light
        // updates warning in the DEFAULT and PERFORMANCE F3 profiles. That is a NeoForge addition
        // with no vanilla counterpart, so the entry is registered but not pre-included.
    }
}
