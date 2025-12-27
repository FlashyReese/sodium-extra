package me.flashyreese.mods.sodiumextra.client.config;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.common.util.ControlValueFormatterExtended;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FogType;

import java.util.*;
import java.util.stream.Collectors;

public class SodiumExtraConfig implements ConfigEntryPoint {
    private static Identifier id(String path) {
        return Identifier.parse("sodium-extra:" + path);
    }

    private static Component parseVanillaString(String key) {
        // Strip formatting codes like "§a"
        return Component.literal(Component.translatable(key).getString().replaceAll("§.", ""));
    }

    private static Component fogTypeName(FogType type) {
        String key = "sodium-extra.option.fog_type." + type.name().toLowerCase();
        Component translated = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translated)) {
            String pretty = Arrays.stream(type.name().split("_"))
                    .map(s -> s.charAt(0) + s.substring(1).toLowerCase())
                    .collect(Collectors.joining(" ")) + " Fog";
            return Component.literal(pretty);
        }
        return translated;
    }

    private static Component fogTypeTooltip(FogType type) {
        String key = "sodium-extra.option.fog_type." + type.name().toLowerCase() + ".tooltip";
        Component translated = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translated)) {
            return Component.translatable("sodium-extra.option.fog_type.default.tooltip", fogTypeName(type));
        }
        return translated;
    }

    private static Component translatableName(Identifier identifier, String category) {
        String key = identifier.toLanguageKey("options.".concat(category));
        Component translatable = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translatable)) {
            translatable = Component.literal(
                    Arrays.stream(key.substring(key.lastIndexOf('.') + 1).split("_"))
                            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                            .collect(Collectors.joining(" "))
            );
        }
        return translatable;
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
        OptionGroupBuilder otherParticlesGroup = builder.createOptionGroup();
        BuiltInRegistries.PARTICLE_TYPE.keySet().stream()
                .sorted((a, b) -> translatableName(a, "particles")
                        .getString()
                        .compareToIgnoreCase(translatableName(b, "particles").getString()))
                .forEach(id -> otherParticlesGroup.addOption(
                        builder.createBooleanOption(id("particle." + id.toLanguageKey("options.particles")))
                                .setName(translatableName(id, "particles"))
                                .setTooltip(translatableTooltip(id, "particles"))
                                .setStorageHandler(SodiumExtraClientMod.options())
                                .setBinding(
                                        value -> SodiumExtraClientMod.options().particleSettings.otherMap.put(id, value),
                                        () -> SodiumExtraClientMod.options().particleSettings.otherMap.computeIfAbsent(id, k -> true)
                                )
                                .setDefaultValue(true)
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                ));

        return builder.createOptionPage()
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
                )

                .addOptionGroup(otherParticlesGroup);
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
        List<OptionGroupBuilder> fogGroups = new ArrayList<>();

        Arrays.stream(FogType.values())
                .sorted(Comparator.comparing(Enum::name))
                .filter(type -> type != FogType.NONE)
                .forEach(fogType -> {
                    // Environment Start
                    IntegerOptionBuilder envStart = builder.createIntegerOption(id(fogType.name().toLowerCase(Locale.ROOT) + "_environment_start"))
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.environment_start", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.environment_start.tooltip"))
                            .setStorageHandler(SodiumExtraClientMod.options())
                            .setRange(0, 300, 1)
                            .setValueFormatter(ControlValueFormatterImpls.percentage())
                            .setDefaultValue(100)
                            .setBinding(val -> {
                                FogTypeConfig config = SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                config.environmentStartMultiplier = val;
                            }, () -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).environmentStartMultiplier)
                            .setValueFormatter(ControlValueFormatterImpls.percentage());

                    // Environment End
                    IntegerOptionBuilder envEnd = builder.createIntegerOption(id(fogType.name().toLowerCase(Locale.ROOT) + "_environment_end"))
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.environment_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.environment_end.tooltip"))
                            .setStorageHandler(SodiumExtraClientMod.options())
                            .setRange(0, 300, 1)
                            .setValueFormatter(ControlValueFormatterImpls.percentage())
                            .setDefaultValue(100)
                            .setBinding(val -> {
                                FogTypeConfig config = SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                config.environmentEndMultiplier = val;
                            }, () -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).environmentEndMultiplier)
                            .setValueFormatter(ControlValueFormatterImpls.percentage());

                    // Render Start
                    IntegerOptionBuilder renderStart = builder.createIntegerOption(id(fogType.name().toLowerCase(Locale.ROOT) + "_render_distance_start"))
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.render_distance_start", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.render_distance_start.tooltip"))
                            .setStorageHandler(SodiumExtraClientMod.options())
                            .setRange(0, 300, 1)
                            .setValueFormatter(ControlValueFormatterImpls.percentage())
                            .setDefaultValue(100)
                            .setBinding(val -> {
                                FogTypeConfig config = SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                config.renderDistanceStartMultiplier = val;
                            }, () -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).renderDistanceStartMultiplier)
                            .setValueFormatter(ControlValueFormatterImpls.percentage());

                    // Render End
                    IntegerOptionBuilder renderEnd = builder.createIntegerOption(id(fogType.name().toLowerCase(Locale.ROOT) + "_render_distance_end"))
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.render_distance_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.render_distance_end.tooltip"))
                            .setStorageHandler(SodiumExtraClientMod.options())
                            .setRange(0, 300, 1)
                            .setValueFormatter(ControlValueFormatterImpls.percentage())
                            .setDefaultValue(100)
                            .setBinding(val -> {
                                FogTypeConfig config = SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                config.renderDistanceEndMultiplier = val;
                            }, () -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).renderDistanceEndMultiplier)
                            .setValueFormatter(ControlValueFormatterImpls.percentage());

                    // Sky End
                    IntegerOptionBuilder skyEnd = builder.createIntegerOption(id(fogType.name().toLowerCase(Locale.ROOT) + "_sky_end"))
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.sky_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.sky_end.tooltip"))
                            .setStorageHandler(SodiumExtraClientMod.options())
                            .setRange(0, 300, 1)
                            .setValueFormatter(ControlValueFormatterImpls.percentage())
                            .setDefaultValue(100)
                            .setBinding(val -> {
                                FogTypeConfig config = SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                config.skyEndMultiplier = val;
                            }, () -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).skyEndMultiplier)
                            .setValueFormatter(ControlValueFormatterImpls.percentage());


                    IntegerOptionBuilder cloudEnd = builder.createIntegerOption(id(fogType.name().toLowerCase(Locale.ROOT) + "_cloud_end"))
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.cloud_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.cloud_end.tooltip"))
                            .setStorageHandler(SodiumExtraClientMod.options())
                            .setRange(0, 300, 1)
                            .setValueFormatter(ControlValueFormatterImpls.percentage())
                            .setDefaultValue(100)
                            .setBinding(val -> {
                                FogTypeConfig config = SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                config.cloudEndMultiplier = val;
                            }, () -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).cloudEndMultiplier)
                            .setValueFormatter(ControlValueFormatterImpls.percentage());

                    // Add group
                    fogGroups.add(builder.createOptionGroup()
                            .addOption(builder.createBooleanOption(id(fogType.name().toLowerCase(Locale.ROOT) + "_fog"))
                                    .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                                    .setName(fogTypeName(fogType))
                                    .setTooltip(fogTypeTooltip(fogType))
                                    .setStorageHandler(SodiumExtraClientMod.options())
                                    .setBinding(
                                            (val) -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).enable = val,
                                            () -> SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).enable
                                    )
                                    .setDefaultValue(true)
                            )
                            .addOption(envStart)
                            .addOption(envEnd)
                            .addOption(renderStart)
                            .addOption(renderEnd)
                            .addOption(skyEnd)
                            .addOption(cloudEnd)
                    );
                });

        OptionPageBuilder page = builder.createOptionPage()
                .setName(Component.translatable("sodium-extra.option.render"));

        page.addOptionGroup(builder.createOptionGroup()
                .addOption(builder.createBooleanOption(id("global_fog"))
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.global_fog"))
                        .setTooltip(Component.translatable("sodium-extra.option.global_fog.tooltip"))
                        .setStorageHandler(SodiumExtraClientMod.options())
                        .setBinding(
                                value -> SodiumExtraClientMod.options().renderSettings.globalFog = value,
                                () -> SodiumExtraClientMod.options().renderSettings.globalFog
                        )
                        .setDefaultValue(true)
                )
        );

        fogGroups.forEach(page::addOptionGroup);

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
                        .addOption(builder.createIntegerOption(id("cloud_height"))
                                .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.cloud").isEnabled())
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

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setIcon(Identifier.parse("sodium-extra:textures/icon.png"))
                .addPage(this.createAnimationsPage(builder))
                .addPage(this.createParticlesPage(builder))
                .addPage(this.createDetailsPage(builder))
                .addPage(this.createRenderPage(builder))
                .addPage(this.createExtraPage(builder))
                .registerOptionReplacement(Identifier.parse("sodium:general.vsync"),
                        builder.createEnumOption(Identifier.parse("sodium:general.vsync"), SodiumExtraGameOptions.VerticalSyncOption.class)
                                .setDefaultValue(SodiumExtraGameOptions.VerticalSyncOption.ON)
                                .setName(Component.translatable("options.vsync"))
                                .setTooltip(Component.literal(Component.translatable("sodium.options.v_sync.tooltip").getString() + "\n- " + Component.translatable("sodium-extra.option.use_adaptive_sync.name").getString() + ": " + Component.translatable("sodium-extra.option.use_adaptive_sync.tooltip").getString()))
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
                                    if (Minecraft.getInstance().options.enableVsync().get() && !SodiumExtraClientMod.options().extraSettings.useAdaptiveSync) {
                                        return SodiumExtraGameOptions.VerticalSyncOption.ON;
                                    } else if (!Minecraft.getInstance().options.enableVsync().get() && !SodiumExtraClientMod.options().extraSettings.useAdaptiveSync) {
                                        return SodiumExtraGameOptions.VerticalSyncOption.OFF;
                                    } else {
                                        return SodiumExtraGameOptions.VerticalSyncOption.ADAPTIVE;
                                    }
                                })
                                .setStorageHandler(() -> {
                                    SodiumExtraClientMod.options().afterSave();
                                    Minecraft.getInstance().options.save();
                                }));
    }
}
