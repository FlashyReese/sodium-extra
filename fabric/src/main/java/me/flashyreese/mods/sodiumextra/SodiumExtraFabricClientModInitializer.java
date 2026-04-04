package me.flashyreese.mods.sodiumextra;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SodiumExtraFabricClientModInitializer implements ClientModInitializer {
    public static void initFabric() {
        SodiumExtraClientMod.registerAll(DebugScreenEntries::register);

        // Fabric-only profile hack stays here
        Identifier lightUpdatesWarning = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.light_updates_warning");

        Map<Identifier, DebugScreenEntryStatus> defaultProfile =
                new HashMap<>(DebugScreenEntries.PROFILES.get(DebugScreenProfile.DEFAULT));
        Map<Identifier, DebugScreenEntryStatus> performanceProfile =
                new HashMap<>(DebugScreenEntries.PROFILES.get(DebugScreenProfile.PERFORMANCE));

        defaultProfile.put(lightUpdatesWarning, DebugScreenEntryStatus.ALWAYS_ON);
        performanceProfile.put(lightUpdatesWarning, DebugScreenEntryStatus.ALWAYS_ON);

        Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> modifiedProfiles =
                new HashMap<>(DebugScreenEntries.PROFILES);
        modifiedProfiles.put(DebugScreenProfile.DEFAULT, Map.copyOf(defaultProfile));
        modifiedProfiles.put(DebugScreenProfile.PERFORMANCE, Map.copyOf(performanceProfile));

        DebugScreenEntries.PROFILES = Collections.unmodifiableMap(modifiedProfiles);
    }

    @Override
    public void onInitializeClient() {
        initFabric();
    }
}
