package me.flashyreese.mods.sodiumextra.client.config;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import me.flashyreese.mods.sodiumextra.client.fog.FogShaderTransformer;
import me.flashyreese.mods.sodiumextra.client.gui.FullscreenResolutionConfirmation;
import me.flashyreese.mods.sodiumextra.common.util.ControlValueFormatterExtended;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.EnumOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.caffeinemc.mods.sodium.client.gui.FullscreenResolutionRange;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class SodiumExtraConfig implements ConfigEntryPoint {
    private static Identifier id(String path) {
        return Identifier.parse("sodium-extra:" + path);
    }

    private static final Identifier ADVANCED_FOG_OPTION_ID = id("advanced_fog_settings");
    private static final Identifier MULTI_DIMENSION_FOG_OPTION_ID = id("multi_dimension_fog");
    private static final Identifier PROTECTED_GAMEPLAY_FOG_OPTION_ID = id("protected_gameplay_fog");
    private static final Identifier WAYLAND_FULLSCREEN_RESOLUTION_OPTION_ID = id("wayland_fullscreen_resolution");
    private static final Identifier CLOUD_HEIGHT_OVERRIDE_OPTION_ID = id("cloud_height_override");
    private static final Identifier SODIUM_FULLSCREEN_OPTION_ID = Identifier.parse("sodium:general.fullscreen");
    private static final Identifier SODIUM_FULLSCREEN_RESOLUTION_OPTION_ID = Identifier.parse("sodium:general.fullscreen_resolution");
    private static final Identifier SODIUM_VSYNC_OPTION_ID = Identifier.parse("sodium:general.vsync");
    private static final List<Identifier> DEFAULT_DIMENSION_EFFECT_IDS = List.of(
            Level.OVERWORLD.identifier(),
            Level.NETHER.identifier(),
            Level.END.identifier()
    );

    private static boolean isFogMixinEnabled() {
        return SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled();
    }

    private static boolean isAdvancedFogOptionEnabled(ConfigState state) {
        return isFogMixinEnabled() && state.readBooleanOption(ADVANCED_FOG_OPTION_ID);
    }

    private static boolean isFogShapeOptionEnabled(ConfigState state) {
        return isAdvancedFogOptionEnabled(state) && FogShaderTransformer.isShapeSupported();
    }

    private static boolean isSingleFogOptionEnabled(ConfigState state) {
        boolean advanced = state.readBooleanOption(ADVANCED_FOG_OPTION_ID);
        return isFogMixinEnabled() && (!advanced || !state.readBooleanOption(MULTI_DIMENSION_FOG_OPTION_ID));
    }

    private static boolean isDimensionFogOptionEnabled(ConfigState state) {
        return isFogMixinEnabled()
                && state.readBooleanOption(ADVANCED_FOG_OPTION_ID)
                && state.readBooleanOption(MULTI_DIMENSION_FOG_OPTION_ID);
    }

    private static boolean isProtectedGameplayFogOptionEnabled(ConfigState state) {
        return isFogMixinEnabled()
                && state.readBooleanOption(ADVANCED_FOG_OPTION_ID)
                && state.readBooleanOption(PROTECTED_GAMEPLAY_FOG_OPTION_ID);
    }

    private static boolean isCloudHeightOptionEnabled(ConfigState state) {
        return SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.cloud").isEnabled()
                && state.readBooleanOption(CLOUD_HEIGHT_OVERRIDE_OPTION_ID);
    }

    private static SodiumExtraGameOptions.FogSettings fogSettings() {
        SodiumExtraGameOptions.RenderSettings renderSettings = SodiumExtraClientMod.options().renderSettings;
        renderSettings.sanitize();
        return renderSettings.fogSettings;
    }

    private static List<Identifier> getDimensionFogEffectIds(SodiumExtraGameOptions.FogSettings fogSettings) {
        // Build the list of dimensions to show options for without creating overrides; an override is
        // only persisted once the player actually edits that dimension's fog distance.
        Set<Identifier> identifiers = new LinkedHashSet<>(DEFAULT_DIMENSION_EFFECT_IDS);
        addKnownWorldDimensionEffectIds(identifiers);
        identifiers.addAll(fogSettings.dimensionOverrides.keySet());

        return identifiers.stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();
    }

    private static void addKnownWorldDimensionEffectIds(Set<Identifier> identifiers) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        if (minecraft.level != null) {
            identifiers.add(getDimensionFogEffectId(minecraft.level));
        }
    }

    private static Identifier getDimensionFogEffectId(Level level) {
        return level.dimensionTypeRegistration()
                .unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(() -> level.dimension().identifier());
    }

    private static void setAtmosphericFogStart(int value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        int clampedValue = Math.clamp(value, 0, 100);
        fogSettings.atmospheric.startPercent = clampedValue;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.startPercent = clampedValue);
    }

    private static void setAtmosphericFogShape(SodiumExtraGameOptions.FogShapeMode value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.shapeMode = value;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.shapeMode = value);
    }

    private static void setAtmosphericSkyFog(boolean value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.affectSkyFog = value;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.affectSkyFog = value);
    }

    private static void setAtmosphericCloudFog(boolean value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.affectCloudFog = value;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.affectCloudFog = value);
    }

    private static int compareParticleNamespace(String a, String b) {
        if (a.equals(Identifier.DEFAULT_NAMESPACE) && !b.equals(Identifier.DEFAULT_NAMESPACE)) {
            return -1;
        }

        if (!a.equals(Identifier.DEFAULT_NAMESPACE) && b.equals(Identifier.DEFAULT_NAMESPACE)) {
            return 1;
        }

        int result = a.compareToIgnoreCase(b);
        return result != 0 ? result : a.compareTo(b);
    }

    private static Component particleNamespaceName(String namespace) {
        String name = Arrays.stream(namespace.split("[^A-Za-z0-9]+"))
                .filter(part -> !part.isBlank())
                .map(SodiumExtraConfig::capitalizeNamespacePart)
                .collect(Collectors.joining(" "));

        return Component.literal(name.isBlank() ? namespace : name);
    }

    private static String capitalizeNamespacePart(String part) {
        return part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT);
    }

    private static Boolean isFullscreenResolutionOptionEnabled(ConfigState state) {
        Monitor monitor = getMonitor();
        if (monitor == null || monitor.getModeCount() <= 0) {
            return false;
        }

        return state.readBooleanOption(SODIUM_FULLSCREEN_OPTION_ID)
                && canUseFullscreenResolution(state);
    }

    private static boolean canUseFullscreenResolution(ConfigState state) {
        return canUseFullscreenResolution(state.readBooleanOption(WAYLAND_FULLSCREEN_RESOLUTION_OPTION_ID));
    }

    private static boolean canUseFullscreenResolution() {
        return canUseFullscreenResolution(SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution);
    }

    private static boolean canUseFullscreenResolution(boolean waylandFullscreenResolution) {
        Util.OS os = Util.getPlatform();
        return os == Util.OS.WINDOWS
                || os == Util.OS.OSX
                || isX11()
                || (isWaylandOrXWayland() && waylandFullscreenResolution);
    }

    private static boolean isX11() {
        return Util.getPlatform() == Util.OS.LINUX
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11
                && !isWaylandSession();
    }

    private static boolean isWaylandOrXWayland() {
        return Util.getPlatform() == Util.OS.LINUX
                && (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND || isWaylandSession());
    }

    private static boolean isWaylandSession() {
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        return System.getenv("WAYLAND_DISPLAY") != null || "wayland".equalsIgnoreCase(sessionType);
    }

    private static Monitor getMonitor() {
        Window window = Minecraft.getInstance().getWindow();
        return window == null ? null : window.findBestMonitor();
    }

    private static Integer getFullscreenResolution() {
        Monitor monitor = getMonitor();
        if (monitor == null) {
            return 0;
        }

        return Minecraft.getInstance().getWindow().getPreferredFullscreenVideoMode()
                .map(monitor::getVideoModeIndex)
                .map(value -> value + 1)
                .orElse(0);
    }

    private static void setFullscreenResolution(Integer value) {
        Monitor monitor = getMonitor();
        if (monitor == null || monitor.getModeCount() <= 0) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        Optional<VideoMode> previousMode = window.getPreferredFullscreenVideoMode();
        if (!canUseFullscreenResolution() || value == 0) {
            window.setPreferredFullscreenVideoMode(Optional.empty());
            SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
            return;
        }

        if (isWaylandOrXWayland()) {
            SodiumExtraClientMod.armWaylandFullscreenResolutionRecovery();
            FullscreenResolutionConfirmation.request(previousMode);
        } else {
            SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
        }

        int modeIndex = Math.clamp(value - 1, 0, monitor.getModeCount() - 1);
        window.setPreferredFullscreenVideoMode(Optional.of(monitor.getMode(modeIndex)));
    }

    private static void clearPreferredFullscreenVideoMode() {
        Window window = Minecraft.getInstance().getWindow();
        if (window != null && window.getPreferredFullscreenVideoMode().isPresent()) {
            window.setPreferredFullscreenVideoMode(Optional.empty());
        }
    }

    private static Component parseVanillaString(String key) {
        // Strip formatting codes like "§a"
        String name = Component.translatable(key).getString().replaceAll("§.", "");
        return Component.literal(name.isBlank() ? fallbackName(key) : name);
    }

    private static Component translatableName(Identifier identifier, String category) {
        String key = identifier.toLanguageKey("options.".concat(category));
        Component translatable = Component.translatable(key);
        String name = translatable.getString().replaceAll("§.", "");

        if (!ComponentUtils.isTranslationResolvable(translatable) || name.isBlank()) {
            return Component.literal(fallbackName(key));
        }

        return Component.literal(name);
    }

    private static String fallbackName(String key) {
        return Arrays.stream(key.substring(key.lastIndexOf('.') + 1).split("_"))
                .filter(s -> !s.isBlank())
                .map(s -> s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static Component translatableTooltip(Identifier identifier, String category) {
        String key = identifier.toLanguageKey("options.".concat(category)).concat(".tooltip");
        Component translatable = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translatable)) {
            translatable = Component.translatable(
                    "sodium-extra.option.".concat(category).concat(".tooltips"),
                    translatableName(identifier, category)
            );
        }
        return translatable;
    }

    private OptionPageBuilder createAnimationsPage(ConfigBuilder builder) {
        return builder.createOptionPage()
                .setName(Component.translatable("sodium-extra.option.animations"))
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("animations_all"))
                                .setName(parseVanillaString("gui.socialInteractions.tab_all"))
                                .setTooltip(Component.translatable("sodium-extra.option.animations_all.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().animationSettings.animation = value, () -> SodiumExtraClientMod.options().animationSettings.animation)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        )
                )
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("animate_water"))
                                .setName(parseVanillaString("block.minecraft.water"))
                                .setTooltip(Component.translatable("sodium-extra.option.animate_water.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().animationSettings.water = value, () -> SodiumExtraClientMod.options().animationSettings.water)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("animate_lava"))
                                .setName(parseVanillaString("block.minecraft.lava"))
                                .setTooltip(Component.translatable("sodium-extra.option.animate_lava.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().animationSettings.lava = value, () -> SodiumExtraClientMod.options().animationSettings.lava)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("animate_fire"))
                                .setName(parseVanillaString("block.minecraft.fire"))
                                .setTooltip(Component.translatable("sodium-extra.option.animate_fire.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().animationSettings.fire = value, () -> SodiumExtraClientMod.options().animationSettings.fire)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("animate_portal"))
                                .setName(parseVanillaString("block.minecraft.nether_portal"))
                                .setTooltip(Component.translatable("sodium-extra.option.animate_portal.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().animationSettings.portal = value, () -> SodiumExtraClientMod.options().animationSettings.portal)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("block_animations"))
                                .setName(Component.translatable("sodium-extra.option.block_animations"))
                                .setTooltip(Component.translatable("sodium-extra.option.block_animations.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().animationSettings.blockAnimations = value, () -> SodiumExtraClientMod.options().animationSettings.blockAnimations)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("animate_sculk_sensor"))
                                .setName(parseVanillaString("block.minecraft.sculk_sensor"))
                                .setTooltip(Component.translatable("sodium-extra.option.animate_sculk_sensor.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().animationSettings.sculkSensor = value, () -> SodiumExtraClientMod.options().animationSettings.sculkSensor)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        )
                );
    }

    private OptionPageBuilder createParticlesPage(ConfigBuilder builder) {
        OptionPageBuilder page = builder.createOptionPage()
                .setName(parseVanillaString("options.particles"))

                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("particles_all"))
                                .setName(parseVanillaString("gui.socialInteractions.tab_all"))
                                .setTooltip(Component.translatable("sodium-extra.option.particles_all.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().particleSettings.particles = value, () -> SodiumExtraClientMod.options().particleSettings.particles)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        )
                )

                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("rain_splash_particles"))
                                .setName(parseVanillaString("subtitles.entity.generic.splash"))
                                .setTooltip(Component.translatable("sodium-extra.option.rain_splash.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().particleSettings.rainSplash = value, () -> SodiumExtraClientMod.options().particleSettings.rainSplash)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("block_break_particles"))
                                .setName(parseVanillaString("subtitles.block.generic.break"))
                                .setTooltip(Component.translatable("sodium-extra.option.block_break.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().particleSettings.blockBreak = value, () -> SodiumExtraClientMod.options().particleSettings.blockBreak)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("block_breaking_particles"))
                                .setName(parseVanillaString("subtitles.block.generic.hit"))
                                .setTooltip(Component.translatable("sodium-extra.option.block_breaking.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(value -> SodiumExtraClientMod.options().particleSettings.blockBreaking = value, () -> SodiumExtraClientMod.options().particleSettings.blockBreaking)
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        )
                );

        Map<String, List<Identifier>> particlesByNamespace = new TreeMap<>(SodiumExtraConfig::compareParticleNamespace);
        BuiltInRegistries.PARTICLE_TYPE.keySet()
                .forEach(identifier -> particlesByNamespace.computeIfAbsent(identifier.getNamespace(), namespace -> new ArrayList<>()).add(identifier));

        particlesByNamespace.forEach((namespace, identifiers) -> {
            OptionGroupBuilder particleGroup = builder.createOptionGroup()
                    .setName(particleNamespaceName(namespace));

            identifiers.stream()
                    .sorted((a, b) -> translatableName(a, "particles")
                            .getString()
                            .compareToIgnoreCase(translatableName(b, "particles").getString()))
                    .forEach(particleId -> particleGroup.addOption(
                            builder.createBooleanOption(id("particle." + particleId.toLanguageKey("options.particles")))
                                    .setName(translatableName(particleId, "particles"))
                                    .setTooltip(translatableTooltip(particleId, "particles"))
                                    .setStorageHandler(SodiumExtraClientMod.options())
                                    .setBinding(
                                            value -> SodiumExtraClientMod.options().particleSettings.otherMap.put(particleId, value),
                                            () -> SodiumExtraClientMod.options().particleSettings.otherMap.computeIfAbsent(particleId, k -> true)
                                    )
                                    .setDefaultValue(true)
                                    .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                    ));

            page.addOptionGroup(particleGroup);
        });

        return page;
    }

    private OptionPageBuilder createDetailsPage(ConfigBuilder builder) {
        return builder.createOptionPage()
                .setName(Component.translatable("sodium-extra.option.details"))

                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("sky"))
                                .setName(Component.translatable("sodium-extra.option.sky"))
                                .setTooltip(Component.translatable("sodium-extra.option.sky.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().detailSettings.sky = value,
                                        () -> SodiumExtraClientMod.options().detailSettings.sky
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("stars"))
                                .setName(Component.translatable("sodium-extra.option.stars"))
                                .setTooltip(Component.translatable("sodium-extra.option.stars.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().detailSettings.stars = value,
                                        () -> SodiumExtraClientMod.options().detailSettings.stars
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("sun"))
                                .setName(Component.translatable("sodium-extra.option.sun"))
                                .setTooltip(Component.translatable("sodium-extra.option.sun.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().detailSettings.sun = value,
                                        () -> SodiumExtraClientMod.options().detailSettings.sun
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("moon"))
                                .setName(Component.translatable("sodium-extra.option.moon"))
                                .setTooltip(Component.translatable("sodium-extra.option.moon.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().detailSettings.moon = value,
                                        () -> SodiumExtraClientMod.options().detailSettings.moon
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("rain_snow"))
                                .setName(parseVanillaString("soundCategory.weather"))
                                .setTooltip(Component.translatable("sodium-extra.option.rain_snow.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().detailSettings.rainSnow = value,
                                        () -> SodiumExtraClientMod.options().detailSettings.rainSnow
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("biome_colors"))
                                .setName(Component.translatable("sodium-extra.option.biome_colors"))
                                .setTooltip(Component.translatable("sodium-extra.option.biome_colors.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().detailSettings.biomeColors = value,
                                        () -> SodiumExtraClientMod.options().detailSettings.biomeColors
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.biome_colors").isEnabled())
                        )
                        .addOption(builder.createBooleanOption(id("sky_colors"))
                                .setName(Component.translatable("sodium-extra.option.sky_colors"))
                                .setTooltip(Component.translatable("sodium-extra.option.sky_colors.tooltip"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().detailSettings.skyColors = value,
                                        () -> SodiumExtraClientMod.options().detailSettings.skyColors
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky_colors").isEnabled())
                        )
                );
    }

    private OptionPageBuilder createRenderPage(ConfigBuilder builder) {
        OptionPageBuilder page = builder.createOptionPage()
                .setName(Component.translatable("sodium-extra.option.render"));

        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        List<Identifier> dimensionFogEffectIds = getDimensionFogEffectIds(fogSettings);

        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(ADVANCED_FOG_OPTION_ID)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.advanced_fog_settings"))
                        .setTooltip(Component.translatable("sodium-extra.option.advanced_fog_settings.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.advanced = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.advanced
                        )
                        .setDefaultValue(false)
                )
                .addOption(builder.createIntegerOption(id("single_fog"))
                        .setEnabledProvider(
                                SodiumExtraConfig::isSingleFogOptionEnabled,
                                ADVANCED_FOG_OPTION_ID,
                                MULTI_DIMENSION_FOG_OPTION_ID
                        )
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.fog_distance"))
                        .setTooltip(Component.translatable("sodium-extra.option.fog_distance.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRangeProvider(FogDistanceHelper::getFogDistanceRange, FogDistanceHelper.SODIUM_RENDER_DISTANCE_OPTION_ID, ConfigState.UPDATE_ON_REBUILD)
                        .setValueFormatter(ControlValueFormatterExtended.fogDistance())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.distanceChunks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.distanceChunks
                        )
                        .setDefaultValue(0)
                )
                .addOption(builder.createIntegerOption(id("fog_start"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.fog_start"))
                        .setTooltip(Component.translatable("sodium-extra.option.fog_start.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRange(0, 100, 1)
                        .setValueFormatter(ControlValueFormatterImpls.percentage())
                        .setBinding(SodiumExtraConfig::setAtmosphericFogStart, () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.startPercent)
                        .setDefaultValue(100)
                )
        );

        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(MULTI_DIMENSION_FOG_OPTION_ID)
                        .setEnabledProvider(SodiumExtraConfig::isAdvancedFogOptionEnabled, ADVANCED_FOG_OPTION_ID)
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.multi_dimension_fog"))
                        .setTooltip(Component.translatable("sodium-extra.option.multi_dimension_fog.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.multiDimensionFogControl = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.multiDimensionFogControl
                        )
                        .setDefaultValue(false)
                )
                .addOption(builder.createEnumOption(id("fog_shape"), SodiumExtraGameOptions.FogShapeMode.class)
                        .setEnabledProvider(SodiumExtraConfig::isFogShapeOptionEnabled, ADVANCED_FOG_OPTION_ID)
                        .setControlHiddenWhenDisabled(false)
                        .setAllowedValues(SodiumExtraGameOptions.FogShapeMode.getAvailableOptions())
                        .setName(Component.translatable("sodium-extra.option.fog_shape"))
                        .setTooltip(Component.translatable("sodium-extra.option.fog_shape.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setBinding(SodiumExtraConfig::setAtmosphericFogShape, () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.shapeMode)
                        .setDefaultValue(SodiumExtraGameOptions.FogShapeMode.VANILLA)
                )
                .addOption(builder.createBooleanOption(id("sky_fog"))
                        .setEnabledProvider(SodiumExtraConfig::isAdvancedFogOptionEnabled, ADVANCED_FOG_OPTION_ID)
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.sky_fog"))
                        .setTooltip(Component.translatable("sodium-extra.option.sky_fog.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setBinding(SodiumExtraConfig::setAtmosphericSkyFog, () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.affectSkyFog)
                        .setDefaultValue(true)
                )
                .addOption(builder.createBooleanOption(id("cloud_fog"))
                        .setEnabledProvider(SodiumExtraConfig::isAdvancedFogOptionEnabled, ADVANCED_FOG_OPTION_ID)
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.cloud_fog"))
                        .setTooltip(Component.translatable("sodium-extra.option.cloud_fog.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setBinding(SodiumExtraConfig::setAtmosphericCloudFog, () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.affectCloudFog)
                        .setDefaultValue(true)
                )
        );

        OptionGroupBuilder dimensionFogGroup = builder.createOptionGroup();
        dimensionFogEffectIds.forEach(identifier -> dimensionFogGroup.addOption(builder.createIntegerOption(id("fog." + identifier.toLanguageKey("options.dimensions")))
                        .setEnabledProvider(
                                SodiumExtraConfig::isDimensionFogOptionEnabled,
                                ADVANCED_FOG_OPTION_ID,
                                MULTI_DIMENSION_FOG_OPTION_ID
                        )
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.fog", translatableName(identifier, "dimensions").getString()))
                        .setTooltip(Component.translatable("sodium-extra.option.fog.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRangeProvider(FogDistanceHelper::getFogDistanceRange, FogDistanceHelper.SODIUM_RENDER_DISTANCE_OPTION_ID, ConfigState.UPDATE_ON_REBUILD)
                        .setValueFormatter(ControlValueFormatterExtended.fogDistance())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.getOrCreateDimensionOverride(identifier).distanceChunks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.getDimensionFogDistance(identifier)
                        )
                        .setDefaultValue(0)
                ));
        if (!dimensionFogEffectIds.isEmpty()) {
            page.addOptionGroup(dimensionFogGroup);
        }

        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(PROTECTED_GAMEPLAY_FOG_OPTION_ID)
                        .setEnabledProvider(SodiumExtraConfig::isAdvancedFogOptionEnabled, ADVANCED_FOG_OPTION_ID)
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.protected_gameplay_fog"))
                        .setTooltip(Component.translatable("sodium-extra.option.protected_gameplay_fog.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.enabledWhenAllowed = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.enabledWhenAllowed
                        )
                        .setDefaultValue(false)
                )
                .addOption(builder.createIntegerOption(id("protected_gameplay_fog.blindness"))
                        .setEnabledProvider(
                                SodiumExtraConfig::isProtectedGameplayFogOptionEnabled,
                                ADVANCED_FOG_OPTION_ID,
                                PROTECTED_GAMEPLAY_FOG_OPTION_ID
                        )
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.protected_gameplay_fog.blindness"))
                        .setTooltip(Component.translatable("sodium-extra.option.protected_gameplay_fog.blindness.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRange(FogDistanceHelper.getProtectedGameplayFogDistanceRange())
                        .setValueFormatter(ControlValueFormatterExtended.protectedFogDistance())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.blindnessDistanceBlocks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.blindnessDistanceBlocks
                        )
                        .setDefaultValue(0)
                )
                .addOption(builder.createIntegerOption(id("protected_gameplay_fog.darkness"))
                        .setEnabledProvider(
                                SodiumExtraConfig::isProtectedGameplayFogOptionEnabled,
                                ADVANCED_FOG_OPTION_ID,
                                PROTECTED_GAMEPLAY_FOG_OPTION_ID
                        )
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.protected_gameplay_fog.darkness"))
                        .setTooltip(Component.translatable("sodium-extra.option.protected_gameplay_fog.darkness.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRange(FogDistanceHelper.getProtectedGameplayFogDistanceRange())
                        .setValueFormatter(ControlValueFormatterExtended.protectedFogDistance())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.darknessDistanceBlocks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.darknessDistanceBlocks
                        )
                        .setDefaultValue(0)
                )
                .addOption(builder.createIntegerOption(id("protected_gameplay_fog.lava"))
                        .setEnabledProvider(
                                SodiumExtraConfig::isProtectedGameplayFogOptionEnabled,
                                ADVANCED_FOG_OPTION_ID,
                                PROTECTED_GAMEPLAY_FOG_OPTION_ID
                        )
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.protected_gameplay_fog.lava"))
                        .setTooltip(Component.translatable("sodium-extra.option.protected_gameplay_fog.lava.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRange(FogDistanceHelper.getProtectedGameplayFogDistanceRange())
                        .setValueFormatter(ControlValueFormatterExtended.protectedFogDistance())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.lavaDistanceBlocks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.lavaDistanceBlocks
                        )
                        .setDefaultValue(0)
                )
                .addOption(builder.createIntegerOption(id("protected_gameplay_fog.powder_snow"))
                        .setEnabledProvider(
                                SodiumExtraConfig::isProtectedGameplayFogOptionEnabled,
                                ADVANCED_FOG_OPTION_ID,
                                PROTECTED_GAMEPLAY_FOG_OPTION_ID
                        )
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.protected_gameplay_fog.powder_snow"))
                        .setTooltip(Component.translatable("sodium-extra.option.protected_gameplay_fog.powder_snow.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRange(FogDistanceHelper.getProtectedGameplayFogDistanceRange())
                        .setValueFormatter(ControlValueFormatterExtended.protectedFogDistance())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.powderSnowDistanceBlocks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.powderSnowDistanceBlocks
                        )
                        .setDefaultValue(0)
                )
                .addOption(builder.createIntegerOption(id("protected_gameplay_fog.water"))
                        .setEnabledProvider(
                                SodiumExtraConfig::isProtectedGameplayFogOptionEnabled,
                                ADVANCED_FOG_OPTION_ID,
                                PROTECTED_GAMEPLAY_FOG_OPTION_ID
                        )
                        .setControlHiddenWhenDisabled(false)
                        .setName(Component.translatable("sodium-extra.option.protected_gameplay_fog.water"))
                        .setTooltip(Component.translatable("sodium-extra.option.protected_gameplay_fog.water.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setRange(FogDistanceHelper.getProtectedGameplayFogDistanceRange())
                        .setValueFormatter(ControlValueFormatterExtended.protectedFogDistance())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.waterDistanceBlocks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.waterDistanceBlocks
                        )
                        .setDefaultValue(0)
                )
        );

        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(id("light_updates"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.light_updates").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.light_updates"))
                        .setTooltip(Component.translatable("sodium-extra.option.light_updates.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.lightUpdates = value, () -> SodiumExtraClientMod.options().renderSettings.lightUpdates)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                ));
        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(id("item_frame"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.item_frame"))
                        .setTooltip(Component.translatable("sodium-extra.option.item_frames.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.itemFrame = value, () -> SodiumExtraClientMod.options().renderSettings.itemFrame)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                )
                .addOption(builder.createBooleanOption(id("armor_stands"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.armor_stand"))
                        .setTooltip(Component.translatable("sodium-extra.option.armor_stands.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.armorStand = value, () -> SodiumExtraClientMod.options().renderSettings.armorStand)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                )
                .addOption(builder.createBooleanOption(id("paintings"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.painting"))
                        .setTooltip(Component.translatable("sodium-extra.option.paintings.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.painting = value, () -> SodiumExtraClientMod.options().renderSettings.painting)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                ));
        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(id("beacon_beam"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.beacon_beam"))
                        .setTooltip(Component.translatable("sodium-extra.option.beacon_beam.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.beaconBeam = value, () -> SodiumExtraClientMod.options().renderSettings.beaconBeam)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                )
                .addOption(builder.createBooleanOption(id("limit_beacon_beam_height"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.limit_beacon_beam_height"))
                        .setTooltip(Component.translatable("sodium-extra.option.limit_beacon_beam_height.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.limitBeaconBeamHeight = value, () -> SodiumExtraClientMod.options().renderSettings.limitBeaconBeamHeight)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(false)
                )
                .addOption(builder.createBooleanOption(id("enchanting_table_book"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.enchanting_table_book"))
                        .setTooltip(Component.translatable("sodium-extra.option.enchanting_table_book.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.enchantingTableBook = value, () -> SodiumExtraClientMod.options().renderSettings.enchantingTableBook)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                )
                .addOption(builder.createBooleanOption(id("piston"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(parseVanillaString("block.minecraft.piston"))
                        .setTooltip(Component.translatable("sodium-extra.option.piston.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.piston = value, () -> SodiumExtraClientMod.options().renderSettings.piston)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                ));
        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(id("item_frame_name_tag"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.item_frame_name_tag"))
                        .setTooltip(Component.translatable("sodium-extra.option.item_frame_name_tag.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.itemFrameNameTag = value, () -> SodiumExtraClientMod.options().renderSettings.itemFrameNameTag)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                )
                .addOption(builder.createBooleanOption(id("player_name_tag"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.player_name_tag"))
                        .setTooltip(Component.translatable("sodium-extra.option.player_name_tag.tooltip"))
                        .setBinding((value) -> SodiumExtraClientMod.options().renderSettings.playerNameTag = value, () -> SodiumExtraClientMod.options().renderSettings.playerNameTag)
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setDefaultValue(true)
                ));


        return page;
    }

    private OptionPageBuilder createExtraPage(ConfigBuilder builder) {
        return builder.createOptionPage()
                .setName(Component.translatable("sodium-extra.option.extras"))

                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("reduce_resolution_on_mac"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.reduce_resolution_on_mac").isEnabled() && System.getProperty("os.name").toLowerCase().contains("mac"))
                                .setName(Component.translatable("sodium-extra.option.reduce_resolution_on_mac"))
                                .setTooltip(Component.translatable("sodium-extra.option.reduce_resolution_on_mac.tooltip"))
                                .setImpact(OptionImpact.HIGH)
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac = value, () -> SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac)
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setDefaultValue(false)
                        )
                        .addOption(builder.createBooleanOption(WAYLAND_FULLSCREEN_RESOLUTION_OPTION_ID)
                                .setEnabled(isWaylandOrXWayland())
                                .setName(Component.translatable("sodium-extra.option.wayland_fullscreen_resolution"))
                                .setTooltip(Component.translatable("sodium-extra.option.wayland_fullscreen_resolution.tooltip"))
                                .setImpact(OptionImpact.MEDIUM)
                                .setBinding((value) -> {
                                    SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution = value;
                                    if (!value) {
                                        clearPreferredFullscreenVideoMode();
                                        SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
                                    }
                                }, () -> SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution)
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setDefaultValue(false)
                        ))
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createEnumOption(id("overlay_corner"), SodiumExtraGameOptions.OverlayCorner.class)
                                .setName(Component.translatable("sodium-extra.option.overlay_corner"))
                                .setTooltip(Component.translatable("sodium-extra.option.overlay_corner.tooltip"))
                                .setDefaultValue(SodiumExtraGameOptions.OverlayCorner.TOP_LEFT)
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.overlayCorner = value, () -> SodiumExtraClientMod.options().extraSettings.overlayCorner)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createEnumOption(id("text_contrast"), SodiumExtraGameOptions.TextContrast.class)
                                .setName(Component.translatable("sodium-extra.option.text_contrast"))
                                .setTooltip(Component.translatable("sodium-extra.option.text_contrast.tooltip"))
                                .setDefaultValue(SodiumExtraGameOptions.TextContrast.NONE)
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.textContrast = value, () -> SodiumExtraClientMod.options().extraSettings.textContrast)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("show_fps"))
                                .setName(Component.translatable("sodium-extra.option.show_fps"))
                                .setTooltip(Component.translatable("sodium-extra.option.show_fps.tooltip"))
                                .setDefaultValue(false)
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.showFps = value, () -> SodiumExtraClientMod.options().extraSettings.showFps)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("show_fps_extended"))
                                .setName(Component.translatable("sodium-extra.option.show_fps_extended"))
                                .setTooltip(Component.translatable("sodium-extra.option.show_fps_extended.tooltip"))
                                .setDefaultValue(true)
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.showFPSExtended = value, () -> SodiumExtraClientMod.options().extraSettings.showFPSExtended)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("show_coordinates"))
                                .setName(Component.translatable("sodium-extra.option.show_coordinates"))
                                .setTooltip(Component.translatable("sodium-extra.option.show_coordinates.tooltip"))
                                .setDefaultValue(false)
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.showCoords = value, () -> SodiumExtraClientMod.options().extraSettings.showCoords)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(CLOUD_HEIGHT_OVERRIDE_OPTION_ID)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.cloud").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.cloud_height_override"))
                                .setTooltip(Component.translatable("sodium-extra.option.cloud_height_override.tooltip"))
                                .setDefaultValue(false)
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.cloudHeightOverride = value, () -> SodiumExtraClientMod.options().extraSettings.cloudHeightOverride)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createIntegerOption(id("cloud_height"))
                                .setEnabledProvider(SodiumExtraConfig::isCloudHeightOptionEnabled, CLOUD_HEIGHT_OVERRIDE_OPTION_ID)
                                .setControlHiddenWhenDisabled(false)
                                .setName(Component.translatable("sodium-extra.option.cloud_height"))
                                .setTooltip(Component.translatable("sodium-extra.option.cloud_height.tooltip"))
                                .setRange(-64, 319, 1)
                                .setDefaultValue(192)
                                .setValueFormatter(ControlValueFormatterImpls.number())
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.cloudHeight = value, () -> SodiumExtraClientMod.options().extraSettings.cloudHeight)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        ))
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("advanced_item_tooltips"))
                                .setName(Component.translatable("sodium-extra.option.advanced_item_tooltips"))
                                .setTooltip(Component.translatable("sodium-extra.option.advanced_item_tooltips.tooltip"))
                                .setStorageHandler(() -> Minecraft.getInstance().options.save())
                                .setBinding((value) -> Minecraft.getInstance().options.advancedItemTooltips = value, () -> Minecraft.getInstance().options.advancedItemTooltips)
                                .setDefaultValue(false)
                        )
                        .addOption(builder.createExternalButtonOption(id("more_hud_overlays"))
                                .setName(Component.translatable("sodium-extra.option.more_hud_overlays"))
                                .setTooltip(Component.translatable("sodium-extra.option.more_hud_overlays.tooltip"))
                                .setScreenConsumer((screen) -> Minecraft.getInstance().setScreenAndShow(new DebugOptionsScreen()))
                        ))
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("toasts"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.toasts"))
                                .setTooltip(Component.translatable("sodium-extra.option.toasts.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.toasts = value, () -> SodiumExtraClientMod.options().extraSettings.toasts)
                                .setDefaultValue(true)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("advancement_toast"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.advancement_toast"))
                                .setTooltip(Component.translatable("sodium-extra.option.advancement_toast.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.advancementToast = value, () -> SodiumExtraClientMod.options().extraSettings.advancementToast)
                                .setDefaultValue(true)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("recipe_toast"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.recipe_toast"))
                                .setTooltip(Component.translatable("sodium-extra.option.recipe_toast.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.recipeToast = value, () -> SodiumExtraClientMod.options().extraSettings.recipeToast)
                                .setDefaultValue(true)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("system_toast"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.system_toast"))
                                .setTooltip(Component.translatable("sodium-extra.option.system_toast.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.systemToast = value, () -> SodiumExtraClientMod.options().extraSettings.systemToast)
                                .setDefaultValue(true)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("tutorial_toast"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.tutorial_toast"))
                                .setTooltip(Component.translatable("sodium-extra.option.tutorial_toast.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.tutorialToast = value, () -> SodiumExtraClientMod.options().extraSettings.tutorialToast)
                                .setDefaultValue(true)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        ))
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("instant_sneak"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.instant_sneak").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.instant_sneak"))
                                .setTooltip(Component.translatable("sodium-extra.option.instant_sneak.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.instantSneak = value, () -> SodiumExtraClientMod.options().extraSettings.instantSneak)
                                .setDefaultValue(false)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createBooleanOption(id("prevent_shaders"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.prevent_shaders").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.prevent_shaders"))
                                .setTooltip(Component.translatable("sodium-extra.option.prevent_shaders.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.preventShaders = value, () -> SodiumExtraClientMod.options().extraSettings.preventShaders)
                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                .setDefaultValue(false)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        ))
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("steady_debug_hud"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.steady_debug_hud"))
                                .setTooltip(Component.translatable("sodium-extra.option.steady_debug_hud.tooltip"))
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.steadyDebugHud = value, () -> SodiumExtraClientMod.options().extraSettings.steadyDebugHud)
                                .setDefaultValue(true)
                                .setStorageHandler(SodiumExtraClientMod.options())
                        )
                        .addOption(builder.createIntegerOption(id("steady_debug_hud_refresh_interval"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                                .setName(Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval"))
                                .setTooltip(Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval.tooltip"))
                                .setRange(1, 20, 1)
                                .setValueFormatter(ControlValueFormatterExtended.ticks())
                                .setDefaultValue(1)
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding((value) -> SodiumExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval = value, () -> SodiumExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval)
                        ));
    }

    private EnumOptionBuilder<SodiumExtraGameOptions.VerticalSyncOption> createVerticalSyncOption(ConfigBuilder builder) {
        EnumSet<SodiumExtraGameOptions.VerticalSyncOption> allowedValues = EnumSet.of(
                SodiumExtraGameOptions.VerticalSyncOption.OFF,
                SodiumExtraGameOptions.VerticalSyncOption.ON
        );
        if (SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.adaptive_sync").isEnabled()
                && SodiumExtraGameOptions.VerticalSyncOption.isAdaptiveSyncSupported()) {
            allowedValues.add(SodiumExtraGameOptions.VerticalSyncOption.ADAPTIVE);
        }

        return builder.createEnumOption(SODIUM_VSYNC_OPTION_ID, SodiumExtraGameOptions.VerticalSyncOption.class)
                .setDefaultValue(SodiumExtraGameOptions.VerticalSyncOption.ON)
                .setAllowedValues(allowedValues)
                .setName(Component.translatable("options.vsync"))
                .setTooltip(Component.literal(Component.translatable("sodium.options.v_sync.tooltip").getString()
                        + "\n- " + Component.translatable("sodium-extra.option.use_adaptive_sync.name").getString()
                        + ": " + Component.translatable("sodium-extra.option.use_adaptive_sync.tooltip").getString()))
                .setBinding((value) -> {
                    switch (value) {
                        case OFF -> {
                            SodiumExtraClientMod.options().extraSettings.useAdaptiveSync = false;
                            Minecraft.getInstance().options.enableVsync().set(false);
                        }
                        case ON -> {
                            SodiumExtraClientMod.options().extraSettings.useAdaptiveSync = false;
                            Minecraft.getInstance().options.enableVsync().set(true);
                        }
                        case ADAPTIVE -> {
                            SodiumExtraClientMod.options().extraSettings.useAdaptiveSync = true;
                            Minecraft.getInstance().options.enableVsync().set(true);
                        }
                    }
                }, () -> {
                    boolean vsync = Minecraft.getInstance().options.enableVsync().get();
                    boolean adaptive = SodiumExtraClientMod.options().extraSettings.useAdaptiveSync
                            && SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.adaptive_sync").isEnabled()
                            && SodiumExtraGameOptions.VerticalSyncOption.isAdaptiveSyncSupported();

                    if (!vsync) {
                        return SodiumExtraGameOptions.VerticalSyncOption.OFF;
                    }

                    return adaptive ? SodiumExtraGameOptions.VerticalSyncOption.ADAPTIVE : SodiumExtraGameOptions.VerticalSyncOption.ON;
                })
                .setStorageHandler(() -> {
                    SodiumExtraClientMod.options().afterSave();
                    Minecraft.getInstance().options.save();
                });
    }

    private IntegerOptionBuilder createFullscreenResolutionOption(ConfigBuilder builder) {
        return builder.createIntegerOption(SODIUM_FULLSCREEN_RESOLUTION_OPTION_ID)
                .setStorageHandler(() -> Minecraft.getInstance().options.save())
                .setName(Component.translatable("options.fullscreen.resolution"))
                .setTooltip(Component.translatable("sodium-extra.option.resolution.tooltip"))
                .setValueFormatter(ControlValueFormatterExtended.resolution())
                .setValidator(new FullscreenResolutionRange())
                .setDefaultValue(0)
                .setBinding(SodiumExtraConfig::setFullscreenResolution, SodiumExtraConfig::getFullscreenResolution)
                .setFlags(OptionFlag.REQUIRES_VIDEOMODE_RELOAD)
                .setEnabledProvider(
                        SodiumExtraConfig::isFullscreenResolutionOptionEnabled,
                        SODIUM_FULLSCREEN_OPTION_ID,
                        WAYLAND_FULLSCREEN_RESOLUTION_OPTION_ID,
                        ConfigState.UPDATE_ON_REBUILD
                );
    }

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setIcon(Identifier.parse("sodium-extra:textures/icon.png"))
                .addPage(this.createAnimationsPage(builder))
                .addPage(this.createParticlesPage(builder))
                .addPage(this.createDetailsPage(builder))
                .addPage(this.createRenderPage(builder))
                .addPage(this.createExtraPage(builder))
                .registerOptionReplacement(SODIUM_FULLSCREEN_RESOLUTION_OPTION_ID, this.createFullscreenResolutionOption(builder))
                .registerOptionReplacement(SODIUM_VSYNC_OPTION_ID, this.createVerticalSyncOption(builder));
    }
}
