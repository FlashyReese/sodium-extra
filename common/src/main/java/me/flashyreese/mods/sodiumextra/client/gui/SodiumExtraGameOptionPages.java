package me.flashyreese.mods.sodiumextra.client.gui;

import com.google.common.collect.ImmutableList;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.gui.options.control.DynamicSliderControl;
import me.flashyreese.mods.sodiumextra.client.gui.options.control.SliderControlExtended;
import me.flashyreese.mods.sodiumextra.client.gui.options.storage.SodiumExtraOptionsStorage;
import me.flashyreese.mods.sodiumextra.common.util.ControlValueFormatterExtended;
import net.caffeinemc.mods.sodium.client.gui.options.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.TickBoxControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SodiumExtraGameOptionPages {
    public static final SodiumExtraOptionsStorage sodiumExtraOpts = new SodiumExtraOptionsStorage();
    public static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();

    private static Component parseVanillaString(String key) {
        return Component.literal((Component.translatable(key).getString()).replaceAll("§.", ""));
    }

    public static OptionPage animation() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("gui.socialInteractions.tab_all"))
                        .setTooltip(Component.translatable("sodium-extra.option.animations_all.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.animation = value, opts -> opts.animationSettings.animation)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.water"))
                        .setTooltip(Component.translatable("sodium-extra.option.animate_water.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.water = value, opts -> opts.animationSettings.water)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.lava"))
                        .setTooltip(Component.translatable("sodium-extra.option.animate_lava.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.lava = value, opts -> opts.animationSettings.lava)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.fire"))
                        .setTooltip(Component.translatable("sodium-extra.option.animate_fire.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.fire = value, opts -> opts.animationSettings.fire)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.nether_portal"))
                        .setTooltip(Component.translatable("sodium-extra.option.animate_portal.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.portal = value, opts -> opts.animationSettings.portal)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.block_animations"))
                        .setTooltip(Component.translatable("sodium-extra.option.block_animations.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.animationSettings.blockAnimations = value, options -> options.animationSettings.blockAnimations)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.sculk_sensor"))
                        .setTooltip(Component.translatable("sodium-extra.option.animate_sculk_sensor.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.animationSettings.sculkSensor = value, options -> options.animationSettings.sculkSensor)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());
        return new OptionPage(Component.translatable("sodium-extra.option.animations"), ImmutableList.copyOf(groups));
    }

    public static OptionPage particle() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("gui.socialInteractions.tab_all"))
                        .setTooltip(Component.translatable("sodium-extra.option.particles_all.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.particles = value, opts -> opts.particleSettings.particles)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("subtitles.entity.generic.splash"))
                        .setTooltip(Component.translatable("sodium-extra.option.rain_splash.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.rainSplash = value, opts -> opts.particleSettings.rainSplash)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("subtitles.block.generic.break"))
                        .setTooltip(Component.translatable("sodium-extra.option.block_break.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreak = value, opts -> opts.particleSettings.blockBreak)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("subtitles.block.generic.hit"))
                        .setTooltip(Component.translatable("sodium-extra.option.block_breaking.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreaking = value, opts -> opts.particleSettings.blockBreaking)
                        .build()
                )
                .build());

        Map<String, List<ResourceLocation>> otherParticles = BuiltInRegistries.PARTICLE_TYPE.keySet().stream()
                .collect(Collectors.groupingBy(ResourceLocation::getNamespace));
        otherParticles.forEach((namespace, identifiers) -> groups.add(identifiers.stream()
                .map(identifier -> OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(translatableName(identifier, "particles"))
                        .setTooltip(translatableTooltip(identifier, "particles"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, val) -> opts.particleSettings.otherMap.put(identifier, val),
                                opts -> opts.particleSettings.otherMap.computeIfAbsent(identifier, k -> true))
                        .build())
                .sorted(Comparator.comparing(o -> o.getName().getString()))
                .collect(
                        OptionGroup::createBuilder,
                        OptionGroup.Builder::add,
                        (b1, b2) -> {
                        }
                ).build()
        ));

        return new OptionPage(parseVanillaString("options.particles"), ImmutableList.copyOf(groups));
    }

    public static OptionPage detail() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.sky"))
                        .setTooltip(Component.translatable("sodium-extra.option.sky.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sky = value, opts -> opts.detailSettings.sky)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.stars").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.stars"))
                        .setTooltip(Component.translatable("sodium-extra.option.stars.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.stars = value, opts -> opts.detailSettings.stars)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sun_moon").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.sun"))
                        .setTooltip(Component.translatable("sodium-extra.option.sun.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sun = value, opts -> opts.detailSettings.sun)
                        .build()
                ).add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sun_moon").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.moon"))
                        .setTooltip(Component.translatable("sodium-extra.option.moon.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.moon = value, opts -> opts.detailSettings.moon)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("soundCategory.weather"))
                        .setTooltip(Component.translatable("sodium-extra.option.rain_snow.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.rainSnow = value, opts -> opts.detailSettings.rainSnow)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.biome_colors").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.biome_colors"))
                        .setTooltip(Component.translatable("sodium-extra.option.biome_colors.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.detailSettings.biomeColors = value, options -> options.detailSettings.biomeColors)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky_colors").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.sky_colors"))
                        .setTooltip(Component.translatable("sodium-extra.option.sky_colors.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.detailSettings.skyColors = value, options -> options.detailSettings.skyColors)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        return new OptionPage(Component.translatable("sodium-extra.option.details"), ImmutableList.copyOf(groups));
    }

    public static OptionPage render() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.global_fog"))
                        .setTooltip(Component.translatable("sodium-extra.option.global_fog.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.globalFog = value, options -> options.renderSettings.globalFog)
                        .build()
                )
                .build());


        Arrays.stream(FogType.values())
                .sorted(Comparator.comparing(Enum::name))
                .filter(type -> type != FogType.NONE)
                .forEach(fogType -> {
                    // Setup cross-references
                    AtomicReference<OptionImpl<SodiumExtraGameOptions, Integer>> environmentStartRef = new AtomicReference<>();
                    AtomicReference<OptionImpl<SodiumExtraGameOptions, Integer>> environmentEndRef = new AtomicReference<>();
                    AtomicReference<OptionImpl<SodiumExtraGameOptions, Integer>> renderDistanceStartRef = new AtomicReference<>();
                    AtomicReference<OptionImpl<SodiumExtraGameOptions, Integer>> renderDistanceEndRef = new AtomicReference<>();

                    // Environment Start
                    OptionImpl<SodiumExtraGameOptions, Integer> envStart = OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.environment_start", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.environment_start.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 300, 1, ControlValueFormatter.percentage(), false))
                            .setBinding(
                                    (opts, val) -> {
                                        FogTypeConfig config = opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                        config.environmentStartMultiplier = val;

                                        // Enforce: end >= start
                                        if (config.environmentEndMultiplier < val) {
                                            config.environmentEndMultiplier = val;
                                            if (environmentEndRef.get() != null) {
                                                environmentEndRef.get().setValue(val);
                                                environmentEndRef.get().applyChanges();
                                            }
                                        }
                                    },
                                    opts -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).environmentStartMultiplier
                            )
                            .build();
                    environmentStartRef.set(envStart);

                    // Environment End
                    OptionImpl<SodiumExtraGameOptions, Integer> envEnd = OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.environment_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.environment_end.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 300, 1, ControlValueFormatter.percentage(), false))
                            .setBinding(
                                    (opts, val) -> {
                                        FogTypeConfig config = opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                        config.environmentEndMultiplier = val;

                                        if (config.environmentStartMultiplier > val) {
                                            config.environmentStartMultiplier = val;
                                            if (environmentStartRef.get() != null) {
                                                environmentStartRef.get().setValue(val);
                                                environmentStartRef.get().applyChanges();
                                            }
                                        }
                                    },
                                    opts -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).environmentEndMultiplier
                            )
                            .build();
                    environmentEndRef.set(envEnd);

                    // Render Start
                    OptionImpl<SodiumExtraGameOptions, Integer> renderStart = OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.render_distance_start", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.render_distance_start.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 300, 1, ControlValueFormatter.percentage(), false))
                            .setBinding(
                                    (opts, val) -> {
                                        FogTypeConfig config = opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                        config.renderDistanceStartMultiplier = val;

                                        // Enforce: end >= start
                                        if (config.renderDistanceEndMultiplier < val) {
                                            config.renderDistanceEndMultiplier = val;
                                            if (renderDistanceEndRef.get() != null) {
                                                renderDistanceEndRef.get().setValue(val);
                                                renderDistanceEndRef.get().applyChanges();
                                            }
                                        }
                                    },
                                    opts -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).renderDistanceStartMultiplier
                            )
                            .build();
                    renderDistanceStartRef.set(renderStart);

                    // Render End
                    OptionImpl<SodiumExtraGameOptions, Integer> renderEnd = OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.render_distance_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.render_distance_end.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 300, 1, ControlValueFormatter.percentage(), false))
                            .setBinding(
                                    (opts, val) -> {
                                        FogTypeConfig config = opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                                        config.renderDistanceEndMultiplier = val;

                                        if (config.renderDistanceStartMultiplier > val) {
                                            config.renderDistanceStartMultiplier = val;
                                            if (renderDistanceStartRef.get() != null) {
                                                renderDistanceStartRef.get().setValue(val);
                                                renderDistanceStartRef.get().applyChanges();
                                            }
                                        }
                                    },
                                    opts -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).renderDistanceEndMultiplier
                            )
                            .build();
                    renderDistanceEndRef.set(renderEnd);

                    OptionImpl<SodiumExtraGameOptions, Integer> skyEnd = OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.sky_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.sky_end.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 300, 1, ControlValueFormatter.percentage(), false))
                            .setBinding(
                                    (opts, val) -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).skyEndMultiplier = val,
                                    opts -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).skyEndMultiplier
                            )
                            .build();

                    OptionImpl<SodiumExtraGameOptions, Integer> cloudEnd = OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Component.translatable("sodium-extra.option.fog_type.cloud_end", fogTypeName(fogType)))
                            .setTooltip(Component.translatable("sodium-extra.option.fog_type.cloud_end.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 300, 1, ControlValueFormatter.percentage(), false))
                            .setBinding(
                                    (opts, val) -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).cloudEndMultiplier = val,
                                    opts -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).cloudEndMultiplier
                            )
                            .build();

                    // Add group
                    groups.add(OptionGroup.createBuilder()
                            .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                                    .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                                    .setName(fogTypeName(fogType))
                                    .setTooltip(fogTypeTooltip(fogType))
                                    .setControl(TickBoxControl::new)
                                    .setBinding(
                                            (opts, val) -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).enable = val,
                                            opts -> opts.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).enable
                                    )
                                    .build())
                            .add(envStart)
                            .add(envEnd)
                            .add(renderStart)
                            .add(renderEnd)
                            .add(skyEnd)
                            .add(cloudEnd)
                            .build());
                });

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.light_updates").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.light_updates"))
                        .setTooltip(Component.translatable("sodium-extra.option.light_updates.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.lightUpdates = value, options -> options.renderSettings.lightUpdates)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.item_frame"))
                        .setTooltip(Component.translatable("sodium-extra.option.item_frames.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrame = value, opts -> opts.renderSettings.itemFrame)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.armor_stand"))
                        .setTooltip(Component.translatable("sodium-extra.option.armor_stands.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.armorStand = value, options -> options.renderSettings.armorStand)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.painting"))
                        .setTooltip(Component.translatable("sodium-extra.option.paintings.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.painting = value, options -> options.renderSettings.painting)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.beacon_beam"))
                        .setTooltip(Component.translatable("sodium-extra.option.beacon_beam.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.beaconBeam = value, opts -> opts.renderSettings.beaconBeam)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.limit_beacon_beam_height"))
                        .setTooltip(Component.translatable("sodium-extra.option.limit_beacon_beam_height.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.limitBeaconBeamHeight = value, opts -> opts.renderSettings.limitBeaconBeamHeight)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.enchanting_table_book"))
                        .setTooltip(Component.translatable("sodium-extra.option.enchanting_table_book.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.enchantingTableBook = value, opts -> opts.renderSettings.enchantingTableBook)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(parseVanillaString("block.minecraft.piston"))
                        .setTooltip(Component.translatable("sodium-extra.option.piston.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.piston = value, options -> options.renderSettings.piston)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.item_frame_name_tag"))
                        .setTooltip(Component.translatable("sodium-extra.option.item_frame_name_tag.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrameNameTag = value, opts -> opts.renderSettings.itemFrameNameTag)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.player_name_tag"))
                        .setTooltip(Component.translatable("sodium-extra.option.player_name_tag.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.playerNameTag = value, options -> options.renderSettings.playerNameTag)
                        .build()
                )
                .build());
        return new OptionPage(Component.translatable("sodium-extra.option.render"), ImmutableList.copyOf(groups));
    }

    public static OptionPage extra() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.reduce_resolution_on_mac").isEnabled() && System.getProperty("os.name").toLowerCase().contains("mac"))
                        .setName(Component.translatable("sodium-extra.option.reduce_resolution_on_mac"))
                        .setTooltip(Component.translatable("sodium-extra.option.reduce_resolution_on_mac.tooltip"))
                        .setEnabled(() -> System.getProperty("os.name").toLowerCase().contains("mac"))
                        .setImpact(OptionImpact.HIGH)
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.reduceResolutionOnMac = value, opts -> opts.extraSettings.reduceResolutionOnMac)
                        .build()
                ).build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(SodiumExtraGameOptions.OverlayCorner.class, sodiumExtraOpts)
                        .setName(Component.translatable("sodium-extra.option.overlay_corner"))
                        .setTooltip(Component.translatable("sodium-extra.option.overlay_corner.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumExtraGameOptions.OverlayCorner.class))
                        .setBinding((opts, value) -> opts.extraSettings.overlayCorner = value, opts -> opts.extraSettings.overlayCorner)
                        .build()
                )
                .add(OptionImpl.createBuilder(SodiumExtraGameOptions.TextContrast.class, sodiumExtraOpts)
                        .setName(Component.translatable("sodium-extra.option.text_contrast"))
                        .setTooltip(Component.translatable("sodium-extra.option.text_contrast.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumExtraGameOptions.TextContrast.class))
                        .setBinding((opts, value) -> opts.extraSettings.textContrast = value, opts -> opts.extraSettings.textContrast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setName(Component.translatable("sodium-extra.option.show_fps"))
                        .setTooltip(Component.translatable("sodium-extra.option.show_fps.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showFps = value, opts -> opts.extraSettings.showFps)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setName(Component.translatable("sodium-extra.option.show_fps_extended"))
                        .setTooltip(Component.translatable("sodium-extra.option.show_fps_extended.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showFPSExtended = value, opts -> opts.extraSettings.showFPSExtended)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setName(Component.translatable("sodium-extra.option.show_coordinates"))
                        .setTooltip(Component.translatable("sodium-extra.option.show_coordinates.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showCoords = value, opts -> opts.extraSettings.showCoords)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.cloud").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.cloud_height"))
                        .setTooltip(Component.translatable("sodium-extra.option.cloud_height.tooltip"))
                        .setControl(option -> new SliderControl(option, -64, 319, 1, ControlValueFormatter.number()))
                        .setBinding((options, value) -> options.extraSettings.cloudHeight = value, options -> options.extraSettings.cloudHeight)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                        .setName(Component.translatable("sodium-extra.option.advanced_item_tooltips"))
                        .setTooltip(Component.translatable("sodium-extra.option.advanced_item_tooltips.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.advancedItemTooltips = value, opts -> opts.advancedItemTooltips)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.toasts"))
                        .setTooltip(Component.translatable("sodium-extra.option.toasts.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.toasts = value, options -> options.extraSettings.toasts)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.advancement_toast"))
                        .setTooltip(Component.translatable("sodium-extra.option.advancement_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.advancementToast = value, options -> options.extraSettings.advancementToast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.recipe_toast"))
                        .setTooltip(Component.translatable("sodium-extra.option.recipe_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.recipeToast = value, options -> options.extraSettings.recipeToast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.system_toast"))
                        .setTooltip(Component.translatable("sodium-extra.option.system_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.systemToast = value, options -> options.extraSettings.systemToast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.tutorial_toast"))
                        .setTooltip(Component.translatable("sodium-extra.option.tutorial_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.tutorialToast = value, options -> options.extraSettings.tutorialToast)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.instant_sneak").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.instant_sneak"))
                        .setTooltip(Component.translatable("sodium-extra.option.instant_sneak.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.instantSneak = value, options -> options.extraSettings.instantSneak)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.prevent_shaders").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.prevent_shaders"))
                        .setTooltip(Component.translatable("sodium-extra.option.prevent_shaders.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.preventShaders = value, options -> options.extraSettings.preventShaders)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.steady_debug_hud"))
                        .setTooltip(Component.translatable("sodium-extra.option.steady_debug_hud.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.steadyDebugHud = value, options -> options.extraSettings.steadyDebugHud)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                        .setEnabled(() -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                        .setName(Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval"))
                        .setTooltip(Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval.tooltip"))
                        .setControl(option -> new SliderControlExtended(option, 1, 20, 1, ControlValueFormatterExtended.ticks(), false))
                        .setBinding((options, value) -> options.extraSettings.steadyDebugHudRefreshInterval = value, options -> options.extraSettings.steadyDebugHudRefreshInterval)
                        .build()
                )
                .build());

        return new OptionPage(Component.translatable("sodium-extra.option.extras"), ImmutableList.copyOf(groups));
    }

    private static Component fogTypeName(FogType type) {
        String key = "sodium-extra.option.fog_type." + type.name().toLowerCase();
        Component translated = Component.translatable(key);

        // Fallback: pretty print from enum name (e.g., DIMENSION_OR_BOSS → "Dimension Or Boss Fog")
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

    private static Component translatableName(ResourceLocation identifier, String category) {
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

    private static Component translatableTooltip(ResourceLocation identifier, String category) {
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
}
