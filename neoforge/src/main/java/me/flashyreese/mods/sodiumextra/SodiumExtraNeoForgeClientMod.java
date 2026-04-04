package me.flashyreese.mods.sodiumextra;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;

@Mod(value = "sodium_extra", dist = Dist.CLIENT)
public class SodiumExtraNeoForgeClientMod {
    public SodiumExtraNeoForgeClientMod(IEventBus bus, ModContainer modContainer) {
        bus.addListener(this::registerDebugEntries);
    }

    private void registerDebugEntries(RegisterDebugEntriesEvent event) {
        SodiumExtraClientMod.registerAll(event::register);

        Identifier lightUpdatesWarning = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.light_updates_warning");
        event.includeInProfile(lightUpdatesWarning, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.ALWAYS_ON);
        event.includeInProfile(lightUpdatesWarning, DebugScreenProfile.PERFORMANCE, DebugScreenEntryStatus.ALWAYS_ON);
    }
}