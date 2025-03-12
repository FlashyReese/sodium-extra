package me.flashyreese.mods.sodiumextra.client;

import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraHud;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraGameOptions;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class SodiumExtraClientMod implements ClientModInitializer {

    private static final ClientTickHandler clientTickHandler = new ClientTickHandler();
    private static SodiumExtraGameOptions CONFIG;
    private static CaffeineConfig MIXIN_CONFIG;
    private static Logger LOGGER;

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
                    .build(FabricLoader.getInstance().getConfigDir().resolve("sodium-extra.properties"));
        }
        return MIXIN_CONFIG;
    }

    public static ClientTickHandler getClientTickHandler() {
        return clientTickHandler;
    }

    private static SodiumExtraGameOptions loadConfig() {
        return SodiumExtraGameOptions.load(FabricLoader.getInstance().getConfigDir().resolve("sodium-extra-options.json").toFile());
    }

    @Override
    public void onInitializeClient() {
        getClientTickHandler().onClientInitialize();
        SodiumExtraHud sodiumExtraHud = new SodiumExtraHud();
        HudRenderCallback.EVENT.register(sodiumExtraHud);
        ClientTickEvents.START_CLIENT_TICK.register(sodiumExtraHud);
    }
}
