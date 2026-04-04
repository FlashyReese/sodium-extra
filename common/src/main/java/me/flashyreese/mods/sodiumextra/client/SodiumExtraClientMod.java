package me.flashyreese.mods.sodiumextra.client;

import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraDebugEntryCoords;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraDebugEntryFps;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraDebugEntryLightUpdates;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraHud;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SodiumExtraClientMod {
    private static SodiumExtraGameOptions CONFIG;
    private static CaffeineConfig MIXIN_CONFIG;
    private static Logger LOGGER;
    private static SodiumExtraHud hud;

    public static Logger logger() {
        if (LOGGER == null) {
            LOGGER = LoggerFactory.getLogger("Sodium Extra");
        }

        return LOGGER;
    }

    public static SodiumExtraGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }

    public static CaffeineConfig mixinConfig() {
        if (MIXIN_CONFIG == null) {
            MIXIN_CONFIG = CaffeineConfig.builder("Sodium Extra").withSettingsKey("sodium-extra:options")
                    .addMixinOption("core", true, false)

                    .addMixinOption("adaptive_sync", true)
                    .addMixinOption("animation", true)
                    .addMixinOption("biome_colors", true)
                    .addMixinOption("cloud", true)
                    .addMixinOption("compat", true, false)
                    .addMixinOption("fog", true)
                    .addMixinOption("fps", true)
                    .addMixinOption("gui", true)
                    .addMixinOption("instant_sneak", true)
                    .addMixinOption("light_updates", true)
                    .addMixinOption("optimizations", true)
                    .addMixinOption("optimizations.beacon_beam_rendering", true)
                    .addMixinOption("particle", true)
                    .addMixinOption("prevent_shaders", true)
                    .addMixinOption("reduce_resolution_on_mac", true)
                    .addMixinOption("render", true)
                    .addMixinOption("render.block", true)
                    .addMixinOption("render.block.entity", true)
                    .addMixinOption("render.entity", true)
                    .addMixinOption("sky", true)
                    .addMixinOption("sky_colors", true)
                    .addMixinOption("stars", true)
                    .addMixinOption("steady_debug_hud", true)
                    .addMixinOption("sun_moon", true)
                    .addMixinOption("toasts", true)

                    //.withLogger(SodiumExtraClientMod.logger())
                    .withInfoUrl("https://github.com/FlashyReese/sodium-extra-fabric/wiki/Configuration-File")
                    .build(PlatformRuntimeInformation.getInstance().getConfigDirectory().resolve("sodium-extra.properties"));
        }
        return MIXIN_CONFIG;
    }

    private static SodiumExtraGameOptions loadConfig() {
        return SodiumExtraGameOptions.load(PlatformRuntimeInformation.getInstance().getConfigDirectory().resolve("sodium-extra-options.json").toFile());
    }

    public static void onTick(Minecraft client) {
        if (hud == null) {
            hud = new SodiumExtraHud();
        }
        hud.onStartTick(client);
    }

    public static void onHudRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (hud == null) {
            hud = new SodiumExtraHud();
        }
        hud.onHudRender(guiGraphics, deltaTracker);
    }

    public static void init() {
        Identifier fps = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.show_fps");
        Identifier coordinates = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.show_coordinates");
        Identifier fpsExtended = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.show_fps_extended");
        Identifier lightUpdatesWarning = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.light_updates_warning");
        DebugScreenEntries.register(fps, new SodiumExtraDebugEntryFps(false));
        DebugScreenEntries.register(coordinates, new SodiumExtraDebugEntryCoords());
        DebugScreenEntries.register(fpsExtended, new SodiumExtraDebugEntryFps(true));
        DebugScreenEntries.register(lightUpdatesWarning, new SodiumExtraDebugEntryLightUpdates());

        // Cursed hack to inject our settings
        Map<Identifier, DebugScreenEntryStatus> defaultProfile = new HashMap<>(DebugScreenEntries.PROFILES.get(DebugScreenProfile.DEFAULT));
        Map<Identifier, DebugScreenEntryStatus> performanceProfile = new HashMap<>(DebugScreenEntries.PROFILES.get(DebugScreenProfile.PERFORMANCE));
        defaultProfile.put(lightUpdatesWarning, DebugScreenEntryStatus.ALWAYS_ON);
        performanceProfile.put(lightUpdatesWarning, DebugScreenEntryStatus.ALWAYS_ON);
        Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> modifiedProfiles = new HashMap<>(DebugScreenEntries.PROFILES);
        modifiedProfiles.put(DebugScreenProfile.DEFAULT, Map.copyOf(defaultProfile));
        modifiedProfiles.put(DebugScreenProfile.PERFORMANCE, Map.copyOf(performanceProfile));
        DebugScreenEntries.PROFILES = Collections.unmodifiableMap(modifiedProfiles);
    }
}
