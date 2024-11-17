package me.flashyreese.mods.sodiumextra.client;

import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraHud;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SodiumExtraClientMod {

    private static final ClientTickHandler clientTickHandler = new ClientTickHandler();
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
                    .addMixinOption("fog_falloff", true)
                    .addMixinOption("gui", true)
                    .addMixinOption("instant_sneak", true)
                    .addMixinOption("light_updates", true)
                    .addMixinOption("optimizations", true)
                    .addMixinOption("optimizations.beacon_beam_rendering", true)
                    .addMixinOption("optimizations.draw_helpers", false)
                    .addMixinOption("optimizations.fast_weather", false)
                    .addMixinOption("particle", true)
                    .addMixinOption("prevent_shaders", true)
                    .addMixinOption("profiler", true)
                    .addMixinOption("reduce_resolution_on_mac", true)
                    .addMixinOption("render", true)
                    .addMixinOption("render.block", true)
                    .addMixinOption("render.block.entity", true)
                    .addMixinOption("render.entity", true)
                    .addMixinOption("sky", true)
                    .addMixinOption("sky_colors", true)
                    .addMixinOption("sodium", true)
                    .addMixinOption("sodium.accessibility", true)
                    .addMixinOption("sodium.fog", true)
                    .addMixinOption("sodium.cloud", true)
                    .addMixinOption("sodium.resolution", true)
                    .addMixinOption("sodium.scrollable_page", true)
                    .addMixinOption("sodium.vsync", true)
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

    public static ClientTickHandler getClientTickHandler() {
        return clientTickHandler;
    }

    private static SodiumExtraGameOptions loadConfig() {
        return SodiumExtraGameOptions.load(PlatformRuntimeInformation.getInstance().getConfigDirectory().resolve("sodium-extra-options.json").toFile());
    }

    public static void onTick(Minecraft client) {
        if (hud == null) {
            hud = new SodiumExtraHud();
        }
        clientTickHandler.onClientTick(client);
        hud.onStartTick(client);
    }

    public static void onHudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (hud == null) {
            hud = new SodiumExtraHud();
        }
        hud.onHudRender(guiGraphics, deltaTracker);
    }
}
