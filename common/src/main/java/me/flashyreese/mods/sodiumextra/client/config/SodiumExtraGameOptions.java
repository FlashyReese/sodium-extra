package me.flashyreese.mods.sodiumextra.client.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import me.flashyreese.mods.sodiumextra.common.util.IdentifierSerializer;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

public class SodiumExtraGameOptions implements StorageEventHandler {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new IdentifierSerializer())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();
    public AnimationSettings animationSettings = new AnimationSettings();
    public ParticleSettings particleSettings = new ParticleSettings();
    public DetailSettings detailSettings = new DetailSettings();
    public RenderSettings renderSettings = new RenderSettings();
    @SerializedName(SodiumExtraConfigKeys.EXTRA_SETTINGS)
    public ExtraSettings extraSettings = new ExtraSettings();
    private Path path;

    public static SodiumExtraGameOptions load(File file) {
        return load(file.toPath());
    }

    public static SodiumExtraGameOptions load(Path path) {
        SodiumExtraGameOptions config;
        boolean shouldWriteChanges = true;

        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config = gson.fromJson(reader, SodiumExtraGameOptions.class);
                if (config == null) {
                    throw new JsonParseException("Root element must be a JSON object");
                }
            } catch (IOException | JsonParseException | IllegalStateException e) {
                SodiumExtraClientMod.logger().warn("Could not read config, falling back to defaults", e);
                config = new SodiumExtraGameOptions();
                shouldWriteChanges = moveCorruptConfig(path);
            }
        } else {
            config = new SodiumExtraGameOptions();
        }

        config.sanitize();
        config.path = path;

        if (shouldWriteChanges) {
            config.writeChanges();
        }

        return config;
    }

    private void sanitize() {
        if (this.animationSettings == null) {
            this.animationSettings = new AnimationSettings();
        }

        if (this.particleSettings == null) {
            this.particleSettings = new ParticleSettings();
        }
        this.particleSettings.sanitize();

        if (this.detailSettings == null) {
            this.detailSettings = new DetailSettings();
        }

        if (this.renderSettings == null) {
            this.renderSettings = new RenderSettings();
        }
        this.renderSettings.sanitize();

        if (this.extraSettings == null) {
            this.extraSettings = new ExtraSettings();
        }
        this.extraSettings.sanitize();
    }

    public void writeChanges() {
        if (this.path == null) {
            SodiumExtraClientMod.logger().warn("Could not save configuration file because no path was set");
            return;
        }

        try {
            this.sanitize();
            ConfigFileIO.writeStringAtomically(this.path, gson.toJson(this) + System.lineSeparator());
        } catch (IOException e) {
            SodiumExtraClientMod.logger().warn("Could not save configuration file", e);
        }
    }

    private static boolean moveCorruptConfig(Path path) {
        try {
            Path corruptPath = ConfigFileIO.moveCorruptFile(path);
            SodiumExtraClientMod.logger().warn("Moved corrupt configuration file to {}", corruptPath);
            return true;
        } catch (IOException e) {
            SodiumExtraClientMod.logger().warn("Could not move corrupt configuration file", e);
            return false;
        }
    }

    @Override
    public void afterSave() {
        this.writeChanges();
    }

    public enum OverlayCorner implements TextProvider {
        TOP_LEFT("sodium-extra.option.overlay_corner.top_left"),
        TOP_RIGHT("sodium-extra.option.overlay_corner.top_right"),
        BOTTOM_LEFT("sodium-extra.option.overlay_corner.bottom_left"),
        BOTTOM_RIGHT("sodium-extra.option.overlay_corner.bottom_right");

        private final Component text;

        OverlayCorner(String text) {
            this.text = Component.translatable(text);
        }

        @Override
        public Component getLocalizedName() {
            return this.text;
        }
    }

    public enum TextContrast implements TextProvider {
        NONE("sodium-extra.option.text_contrast.none"),
        BACKGROUND("sodium-extra.option.text_contrast.background"),
        SHADOW("sodium-extra.option.text_contrast.shadow");

        private final Component text;

        TextContrast(String text) {
            this.text = Component.translatable(text);
        }

        @Override
        public Component getLocalizedName() {
            return this.text;
        }
    }

    public enum VerticalSyncOption implements TextProvider {
        OFF("options.off"),
        ON("options.on"),
        ADAPTIVE("sodium-extra.option.use_adaptive_sync.name");

        private final Component name;

        VerticalSyncOption(String name) {
            this.name = Component.translatable(name);
        }

        public static VerticalSyncOption[] getAvailableOptions() {
            return Arrays.stream(VerticalSyncOption.values()).filter(VerticalSyncOption::isSupported).toArray(VerticalSyncOption[]::new);
        }

        public static boolean isAdaptiveSyncSupported() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.getWindow() == null) {
                return false;
            }

            return GLFW.glfwGetCurrentContext() != 0L
                    && (GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear") || GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear"));
        }

        private boolean isSupported() {
            return this != ADAPTIVE || isAdaptiveSyncSupported();
        }

        @Override
        public Component getLocalizedName() {
            return this.name;
        }
    }

    public static class AnimationSettings {
        public boolean animation;
        public boolean water;
        public boolean lava;
        public boolean fire;
        public boolean portal;
        public boolean blockAnimations;
        public boolean sculkSensor;

        public AnimationSettings() {
            this.animation = true;
            this.water = true;
            this.lava = true;
            this.fire = true;
            this.portal = true;
            this.blockAnimations = true;
            this.sculkSensor = true;
        }
    }

    public static class ParticleSettings {
        public boolean particles;
        public boolean rainSplash;
        public boolean blockBreak;
        public boolean blockBreaking;
        @SerializedName("other")
        public Map<ResourceLocation, Boolean> otherMap;

        public ParticleSettings() {
            this.particles = true;
            this.rainSplash = true;
            this.blockBreak = true;
            this.blockBreaking = true;
            this.otherMap = new Object2BooleanArrayMap<>();
        }

        public void sanitize() {
            if (this.otherMap == null) {
                this.otherMap = new Object2BooleanArrayMap<>();
            }
        }

        public boolean isParticleEnabled(ResourceLocation particleTypeId) {
            this.sanitize();
            return this.particles && this.otherMap.computeIfAbsent(particleTypeId, k -> true);
        }
    }

    public static class DetailSettings {
        public boolean sky;
        public boolean sun;
        public boolean moon;
        public boolean stars;
        public boolean rainSnow;
        public boolean biomeColors;
        public boolean skyColors;

        public DetailSettings() {
            this.sky = true;
            this.sun = true;
            this.moon = true;
            this.stars = true;
            this.rainSnow = true;
            this.biomeColors = true;
            this.skyColors = true;
        }
    }

    public static class RenderSettings {
        public FogSettings fogSettings;
        public boolean lightUpdates;
        public boolean itemFrame;
        public boolean armorStand;
        public boolean painting;
        public boolean piston;
        public boolean beaconBeam;
        public boolean limitBeaconBeamHeight;
        public boolean enchantingTableBook;
        public boolean itemFrameNameTag;
        public boolean playerNameTag;

        public RenderSettings() {
            this.fogSettings = new FogSettings();
            this.lightUpdates = true;
            this.itemFrame = true;
            this.armorStand = true;
            this.painting = true;
            this.piston = true;
            this.beaconBeam = true;
            this.limitBeaconBeamHeight = false;
            this.enchantingTableBook = true;
            this.itemFrameNameTag = true;
            this.playerNameTag = true;
        }

        public void sanitize() {
            if (this.fogSettings == null) {
                this.fogSettings = new FogSettings();
            }

            this.fogSettings.sanitize();
        }

    }

    public enum FogShapeMode implements TextProvider {
        VANILLA("sodium-extra.option.fog_shape.vanilla"),
        CYLINDRICAL("sodium-extra.option.fog_shape.cylindrical"),
        RADIAL("sodium-extra.option.fog_shape.radial"),
        PLANAR("sodium-extra.option.fog_shape.planar");

        private final Component text;

        FogShapeMode(String text) {
            this.text = Component.translatable(text);
        }

        public static EnumSet<FogShapeMode> getAvailableOptions() {
            return EnumSet.of(VANILLA, CYLINDRICAL, RADIAL, PLANAR);
        }

        @Override
        public Component getLocalizedName() {
            return this.text;
        }
    }

    public static class FogSettings {
        public boolean advanced;
        public boolean multiDimensionFogControl;
        public AtmosphericFogSettings atmospheric;
        public Map<ResourceLocation, AtmosphericFogSettings> dimensionOverrides;
        public ProtectedFogSettings protectedGameplay;

        public FogSettings() {
            this.advanced = false;
            this.multiDimensionFogControl = false;
            this.atmospheric = new AtmosphericFogSettings();
            this.dimensionOverrides = new Object2ObjectArrayMap<>();
            this.protectedGameplay = new ProtectedFogSettings();
        }

        public void sanitize() {
            if (this.atmospheric == null) {
                this.atmospheric = new AtmosphericFogSettings();
            }
            this.atmospheric.sanitize();

            if (this.dimensionOverrides == null) {
                this.dimensionOverrides = new Object2ObjectArrayMap<>();
            }

            Map<ResourceLocation, AtmosphericFogSettings> sanitizedDimensionOverrides = new Object2ObjectArrayMap<>(this.dimensionOverrides.size());
            for (Map.Entry<ResourceLocation, AtmosphericFogSettings> entry : this.dimensionOverrides.entrySet()) {
                ResourceLocation identifier = entry.getKey();
                if (identifier == null) {
                    continue;
                }

                AtmosphericFogSettings settings = entry.getValue();
                if (settings == null) {
                    settings = new AtmosphericFogSettings();
                }

                settings.sanitize();
                sanitizedDimensionOverrides.put(identifier, settings);
            }
            this.dimensionOverrides = sanitizedDimensionOverrides;

            if (this.protectedGameplay == null) {
                this.protectedGameplay = new ProtectedFogSettings();
            }
            this.protectedGameplay.sanitize();
        }

        public AtmosphericFogSettings getAtmospheric(ResourceLocation dimensionId) {
            if (!this.advanced || !this.multiDimensionFogControl) {
                return this.atmospheric;
            }

            return this.getOrCreateDimensionOverride(dimensionId);
        }

        public AtmosphericFogSettings getOrCreateDimensionOverride(ResourceLocation dimensionId) {
            AtmosphericFogSettings settings = this.dimensionOverrides.computeIfAbsent(dimensionId, ignored -> new AtmosphericFogSettings());
            settings.sanitize();
            return settings;
        }
    }

    public static class AtmosphericFogSettings {
        public int distanceChunks;
        public int startPercent;
        public FogShapeMode shapeMode;
        public boolean affectSkyFog;
        public boolean affectCloudFog;

        public AtmosphericFogSettings() {
            this.distanceChunks = FogDistanceHelper.FOG_DISTANCE_VANILLA;
            this.startPercent = 100;
            this.shapeMode = FogShapeMode.VANILLA;
            this.affectSkyFog = true;
            this.affectCloudFog = true;
        }

        public void sanitize() {
            this.distanceChunks = FogDistanceHelper.normalizeFogDistance(this.distanceChunks);
            this.startPercent = Math.max(0, Math.min(this.startPercent, 100));

            if (this.shapeMode == null || !FogShapeMode.getAvailableOptions().contains(this.shapeMode)) {
                this.shapeMode = FogShapeMode.VANILLA;
            }
        }
    }

    public static class ProtectedFogSettings {
        @SerializedName(value = "enabled_when_allowed", alternate = "enabled_in_private_singleplayer")
        public boolean enabledWhenAllowed;
        @SerializedName(value = "blindness_distance_blocks", alternate = "blindness_distance_chunks")
        public int blindnessDistanceBlocks;
        @SerializedName(value = "darkness_distance_blocks", alternate = "darkness_distance_chunks")
        public int darknessDistanceBlocks;
        @SerializedName(value = "lava_distance_blocks", alternate = "lava_distance_chunks")
        public int lavaDistanceBlocks;
        @SerializedName(value = "powder_snow_distance_blocks", alternate = "powder_snow_distance_chunks")
        public int powderSnowDistanceBlocks;
        public int waterDistanceBlocks;

        public ProtectedFogSettings() {
            this.enabledWhenAllowed = false;
            this.blindnessDistanceBlocks = FogDistanceHelper.FOG_DISTANCE_VANILLA;
            this.darknessDistanceBlocks = FogDistanceHelper.FOG_DISTANCE_VANILLA;
            this.lavaDistanceBlocks = FogDistanceHelper.FOG_DISTANCE_VANILLA;
            this.powderSnowDistanceBlocks = FogDistanceHelper.FOG_DISTANCE_VANILLA;
            this.waterDistanceBlocks = FogDistanceHelper.FOG_DISTANCE_VANILLA;
        }

        public void sanitize() {
            this.blindnessDistanceBlocks = FogDistanceHelper.normalizeFogDistance(this.blindnessDistanceBlocks);
            this.darknessDistanceBlocks = FogDistanceHelper.normalizeFogDistance(this.darknessDistanceBlocks);
            this.lavaDistanceBlocks = FogDistanceHelper.normalizeFogDistance(this.lavaDistanceBlocks);
            this.powderSnowDistanceBlocks = FogDistanceHelper.normalizeFogDistance(this.powderSnowDistanceBlocks);
            this.waterDistanceBlocks = FogDistanceHelper.normalizeFogDistance(this.waterDistanceBlocks);
        }
    }

    public static class ExtraSettings {
        public OverlayCorner overlayCorner;
        public TextContrast textContrast;
        public boolean showFps;
        public boolean showFPSExtended;
        public boolean showCoords;
        public boolean reduceResolutionOnMac;
        @SerializedName(SodiumExtraConfigKeys.WAYLAND_FULLSCREEN_RESOLUTION)
        public boolean waylandFullscreenResolution;
        @SerializedName(SodiumExtraConfigKeys.WAYLAND_FULLSCREEN_RESOLUTION_RECOVERY_PENDING)
        public boolean waylandFullscreenResolutionRecoveryPending;
        public boolean useAdaptiveSync;
        public boolean cloudHeightOverride;
        public int cloudHeight;
        public int cloudDistance;
        public boolean toasts;
        public boolean advancementToast;
        public boolean recipeToast;
        public boolean systemToast;
        public boolean tutorialToast;
        public boolean instantSneak;
        public boolean preventShaders;
        public boolean steadyDebugHud;
        public int steadyDebugHudRefreshInterval;

        public ExtraSettings() {
            this.overlayCorner = OverlayCorner.TOP_LEFT;
            this.textContrast = TextContrast.NONE;
            this.showFps = false;
            this.showFPSExtended = true;
            this.showCoords = false;
            this.reduceResolutionOnMac = false;
            this.waylandFullscreenResolution = false;
            this.waylandFullscreenResolutionRecoveryPending = false;
            this.useAdaptiveSync = false;
            this.cloudHeightOverride = false;
            this.cloudHeight = 192;
            this.cloudDistance = 100;
            this.toasts = true;
            this.advancementToast = true;
            this.recipeToast = true;
            this.systemToast = true;
            this.tutorialToast = true;
            this.instantSneak = false;
            this.preventShaders = false;
            this.steadyDebugHud = true;
            this.steadyDebugHudRefreshInterval = 1;
        }

        public void sanitize() {
            if (this.overlayCorner == null) {
                this.overlayCorner = OverlayCorner.TOP_LEFT;
            }

            if (this.textContrast == null) {
                this.textContrast = TextContrast.NONE;
            }

            if (this.steadyDebugHudRefreshInterval < 1) {
                this.steadyDebugHudRefreshInterval = 1;
            }
        }
    }
}
