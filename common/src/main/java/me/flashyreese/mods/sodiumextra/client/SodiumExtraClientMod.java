package me.flashyreese.mods.sodiumextra.client;

import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.hud.HudManager;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraDebugEntryCoords;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraDebugEntryFps;
import me.flashyreese.mods.sodiumextra.client.gui.SodiumExtraDebugEntryLightUpdates;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.caffeinemc.mods.sodium.client.services.PlatformRuntimeInformation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class SodiumExtraClientMod {
    private static SodiumExtraGameOptions CONFIG;
    private static CaffeineConfig MIXIN_CONFIG;
    private static Logger LOGGER;
    private static HudManager hudManager;

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
        hudManager().tick();
    }

    public static void onHudRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        hudManager().render(guiGraphics, deltaTracker);
    }

    public static HudManager hudManager() {
        if (hudManager == null) {
            hudManager = new HudManager();
        }
        return hudManager;
    }

    public static void registerAll(BiConsumer<Identifier, DebugScreenEntry> register) {
        Identifier fps = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.show_fps");
        Identifier coordinates = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.show_coordinates");
        Identifier fpsExtended = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.show_fps_extended");
        Identifier lightUpdatesWarning = Identifier.fromNamespaceAndPath("sodium-extra", "sodium-extra.option.light_updates_warning");

        register.accept(fps, new SodiumExtraDebugEntryFps(false));
        register.accept(coordinates, new SodiumExtraDebugEntryCoords());
        register.accept(fpsExtended, new SodiumExtraDebugEntryFps(true));
        register.accept(lightUpdatesWarning, new SodiumExtraDebugEntryLightUpdates());
    }
}
