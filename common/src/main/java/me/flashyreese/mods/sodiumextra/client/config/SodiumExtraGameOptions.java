package me.flashyreese.mods.sodiumextra.client.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.common.util.IdentifierSerializer;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.client.gui.options.TextProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FogType;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class SodiumExtraGameOptions implements StorageEventHandler {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();
    public final AnimationSettings animationSettings = new AnimationSettings();
    public final ParticleSettings particleSettings = new ParticleSettings();
    public final DetailSettings detailSettings = new DetailSettings();
    public final RenderSettings renderSettings = new RenderSettings();
    public final ExtraSettings extraSettings = new ExtraSettings();
    private File file;

    public static SodiumExtraGameOptions load(File file) {
        SodiumExtraGameOptions config;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = gson.fromJson(reader, SodiumExtraGameOptions.class);
            } catch (Exception e) {
                SodiumExtraClientMod.logger().error("Could not parse config, falling back to defaults!", e);
                config = new SodiumExtraGameOptions();
            }
        } else {
            config = new SodiumExtraGameOptions();
        }

        config.file = file;
        config.writeChanges();

        return config;
    }

    public void writeChanges() {
        File dir = this.file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("The parent file is not a directory");
        }

        try (FileWriter writer = new FileWriter(this.file)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save configuration file", e);
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
        ADAPTIVE("sodium-extra.option.use_adaptive_sync.name", GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear") || GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear"));

        private final Component name;
        private final boolean supported;

        VerticalSyncOption(String name) {
            this(name, true);
        }

        VerticalSyncOption(String name, boolean supported) {
            this.name = Component.translatable(name);
            this.supported = supported;
        }

        public static VerticalSyncOption[] getAvailableOptions() {
            return Arrays.stream(VerticalSyncOption.values()).filter((o) -> o.supported).toArray(VerticalSyncOption[]::new);
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
        public Map<Identifier, Boolean> otherMap;

        public ParticleSettings() {
            this.particles = true;
            this.rainSplash = true;
            this.blockBreak = true;
            this.blockBreaking = true;
            this.otherMap = new Object2BooleanArrayMap<>();
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
        public boolean globalFog;
        public EnumMap<FogType, FogTypeConfig> fogTypeConfig;
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
            this.globalFog = true;
            this.fogTypeConfig = new EnumMap<>(FogType.class);
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

            this.ensureFogTypeDefaults();
        }

        public void ensureFogTypeDefaults() {
            for (FogType type : FogType.values()) {
                if (type == FogType.NONE) continue;
                this.fogTypeConfig.putIfAbsent(type, new FogTypeConfig());
            }
        }

    }

    public static class ExtraSettings {
        public OverlayCorner overlayCorner;
        public TextContrast textContrast;
        public boolean showFps;
        public boolean showFPSExtended;
        public boolean showCoords;
        public boolean reduceResolutionOnMac;
        public boolean useAdaptiveSync;
        public int cloudHeight;
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
            this.useAdaptiveSync = false;
            this.cloudHeight = 192;
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
    }
}
