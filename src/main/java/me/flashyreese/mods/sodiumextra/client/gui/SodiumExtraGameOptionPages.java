package me.flashyreese.mods.sodiumextra.client.gui;

import com.google.common.collect.ImmutableList;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.gui.options.control.SliderControlExtended;
import me.flashyreese.mods.sodiumextra.client.gui.options.storage.SodiumExtraOptionsStorage;
import me.flashyreese.mods.sodiumextra.common.util.ControlValueFormatterExtended;
import me.jellysquid.mods.sodium.client.gui.options.*;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SodiumExtraGameOptionPages {
    public static final SodiumExtraOptionsStorage sodiumExtraOpts = new SodiumExtraOptionsStorage();
    public static final MinecraftOptionsStorage vanillaOpts = new MinecraftOptionsStorage();

    private static Text parseVanillaString(String key) {
        return Text.literal((Text.translatable(key).getString()).replaceAll("§.", ""));
    }

    public static OptionPage animation() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("gui.socialInteractions.tab_all"))
                        .setTooltip(Text.translatable("sodium-extra.option.animations_all.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.animation = value, opts -> opts.animationSettings.animation)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.water"))
                        .setTooltip(Text.translatable("sodium-extra.option.animate_water.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.water = value, opts -> opts.animationSettings.water)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.lava"))
                        .setTooltip(Text.translatable("sodium-extra.option.animate_lava.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.lava = value, opts -> opts.animationSettings.lava)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.fire"))
                        .setTooltip(Text.translatable("sodium-extra.option.animate_fire.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.fire = value, opts -> opts.animationSettings.fire)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.nether_portal"))
                        .setTooltip(Text.translatable("sodium-extra.option.animate_portal.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.animationSettings.portal = value, opts -> opts.animationSettings.portal)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.block_animations"))
                        .setTooltip(Text.translatable("sodium-extra.option.block_animations.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.animationSettings.blockAnimations = value, options -> options.animationSettings.blockAnimations)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled())
                        .setName(parseVanillaString("block.minecraft.sculk_sensor"))
                        .setTooltip(Text.translatable("sodium-extra.option.animate_sculk_sensor.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.animationSettings.sculkSensor = value, options -> options.animationSettings.sculkSensor)
                        .setFlags(OptionFlag.REQUIRES_ASSET_RELOAD)
                        .build()
                )
                .build());
        return new OptionPage(Text.translatable("sodium-extra.option.animations"), ImmutableList.copyOf(groups));
    }

    public static OptionPage particle() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("gui.socialInteractions.tab_all"))
                        .setTooltip(Text.translatable("sodium-extra.option.particles_all.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.particles = value, opts -> opts.particleSettings.particles)
                        .build()
                )
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("subtitles.entity.generic.splash"))
                        .setTooltip(Text.translatable("sodium-extra.option.rain_splash.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.rainSplash = value, opts -> opts.particleSettings.rainSplash)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("subtitles.block.generic.break"))
                        .setTooltip(Text.translatable("sodium-extra.option.block_break.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreak = value, opts -> opts.particleSettings.blockBreak)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("subtitles.block.generic.hit"))
                        .setTooltip(Text.translatable("sodium-extra.option.block_breaking.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.particleSettings.blockBreaking = value, opts -> opts.particleSettings.blockBreaking)
                        .build()
                )
                .build());

        Map<String, List<Identifier>> otherParticles = Registries.PARTICLE_TYPE.getIds().stream()
                .collect(Collectors.groupingBy(Identifier::getNamespace));
        otherParticles.forEach((namespace, identifiers) -> groups.add(identifiers.stream()
                .map(identifier -> OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(translatableName(identifier, "particles"))
                        .setTooltip(translatableTooltip(identifier, "particles"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, val) -> opts.particleSettings.otherMap.put(identifier, val),
                                opts -> opts.particleSettings.otherMap.getOrDefault(identifier, true))
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
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.sky"))
                        .setTooltip(Text.translatable("sodium-extra.option.sky.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sky = value, opts -> opts.detailSettings.sky)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.stars").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.stars"))
                        .setTooltip(Text.translatable("sodium-extra.option.stars.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.stars = value, opts -> opts.detailSettings.stars)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sun_moon").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.sun_moon"))
                        .setTooltip(Text.translatable("sodium-extra.option.sun_moon.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.sunMoon = value, opts -> opts.detailSettings.sunMoon)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled())
                        .setName(parseVanillaString("soundCategory.weather"))
                        .setTooltip(Text.translatable("sodium-extra.option.rain_snow.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.detailSettings.rainSnow = value, opts -> opts.detailSettings.rainSnow)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.biome_colors").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.biome_colors"))
                        .setTooltip(Text.translatable("sodium-extra.option.biome_colors.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.detailSettings.biomeColors = value, options -> options.detailSettings.biomeColors)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky_colors").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.sky_colors"))
                        .setTooltip(Text.translatable("sodium-extra.option.sky_colors.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.detailSettings.skyColors = value, options -> options.detailSettings.skyColors)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        return new OptionPage(Text.translatable("sodium-extra.option.details"), ImmutableList.copyOf(groups));
    }

    public static OptionPage render() {
        List<OptionGroup> groups = new ArrayList<>();

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.multi_dimension_fog"))
                        .setTooltip(Text.translatable("sodium-extra.option.multi_dimension_fog.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.multiDimensionFogControl = value, options -> options.renderSettings.multiDimensionFogControl)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog_falloff").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.fog_start"))
                        .setTooltip(Text.translatable("sodium-extra.option.fog_start.tooltip"))
                        .setControl(option -> new SliderControlExtended(option, 20, 100, 1, ControlValueFormatter.percentage(), false))
                        .setBinding((options, value) -> options.renderSettings.fogStart = value, options -> options.renderSettings.fogStart)
                        .build()
                )
                .build());

        if (SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl) {
            DimensionOptionsRegistryHolder
                    .streamAll(Stream.empty())
                    .filter(dim -> !SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.containsKey(dim.getValue()))
                    .forEach(dim -> SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.put(dim.getValue(), 0));
            groups.add(SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.keySet().stream()
                    .map(identifier -> OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Text.translatable("sodium-extra.option.fog", translatableName(identifier, "dimensions").getString()))
                            .setTooltip(Text.translatable("sodium-extra.option.fog.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 33, 1, ControlValueFormatterExtended.fogDistance(), false))
                            .setBinding((opts, val) -> opts.renderSettings.dimensionFogDistanceMap.put(identifier, val),
                                    opts -> opts.renderSettings.dimensionFogDistanceMap.getOrDefault(identifier, 0))
                            .build()
                    ).collect(
                            OptionGroup::createBuilder,
                            OptionGroup.Builder::add,
                            (b1, b2) -> {
                            }
                    ).build()
            );
        } else {
            groups.add(OptionGroup.createBuilder()
                    .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                            .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled())
                            .setName(Text.translatable("sodium-extra.option.single_fog"))
                            .setTooltip(Text.translatable("sodium-extra.option.single_fog.tooltip"))
                            .setControl(option -> new SliderControlExtended(option, 0, 33, 1, ControlValueFormatterExtended.fogDistance(), false))
                            .setBinding((options, value) -> options.renderSettings.fogDistance = value, options -> options.renderSettings.fogDistance)
                            .build()
                    )
                    .build());
        }

        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.light_updates").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.light_updates"))
                        .setTooltip(Text.translatable("sodium-extra.option.light_updates.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.lightUpdates = value, options -> options.renderSettings.lightUpdates)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.item_frame"))
                        .setTooltip(Text.translatable("sodium-extra.option.item_frames.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrame = value, opts -> opts.renderSettings.itemFrame)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.armor_stand"))
                        .setTooltip(Text.translatable("sodium-extra.option.armor_stands.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.armorStand = value, options -> options.renderSettings.armorStand)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(parseVanillaString("entity.minecraft.painting"))
                        .setTooltip(Text.translatable("sodium-extra.option.paintings.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.painting = value, options -> options.renderSettings.painting)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.beacon_beam"))
                        .setTooltip(Text.translatable("sodium-extra.option.beacon_beam.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.beaconBeam = value, opts -> opts.renderSettings.beaconBeam)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.limit_beacon_beam_height"))
                        .setTooltip(Text.translatable("sodium-extra.option.limit_beacon_beam_height.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.limitBeaconBeamHeight = value, opts -> opts.renderSettings.limitBeaconBeamHeight)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.enchanting_table_book"))
                        .setTooltip(Text.translatable("sodium-extra.option.enchanting_table_book.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.enchantingTableBook = value, opts -> opts.renderSettings.enchantingTableBook)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled())
                        .setName(parseVanillaString("block.minecraft.piston"))
                        .setTooltip(Text.translatable("sodium-extra.option.piston.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.piston = value, options -> options.renderSettings.piston)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.item_frame_name_tag"))
                        .setTooltip(Text.translatable("sodium-extra.option.item_frame_name_tag.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.renderSettings.itemFrameNameTag = value, opts -> opts.renderSettings.itemFrameNameTag)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.player_name_tag"))
                        .setTooltip(Text.translatable("sodium-extra.option.player_name_tag.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.renderSettings.playerNameTag = value, options -> options.renderSettings.playerNameTag)
                        .build()
                )
                .build());
        return new OptionPage(Text.translatable("sodium-extra.option.render"), ImmutableList.copyOf(groups));
    }

    public static OptionPage extra() {
        List<OptionGroup> groups = new ArrayList<>();
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.reduce_resolution_on_mac").isEnabled() && MinecraftClient.IS_SYSTEM_MAC)
                        .setName(Text.translatable("sodium-extra.option.reduce_resolution_on_mac"))
                        .setTooltip(Text.translatable("sodium-extra.option.reduce_resolution_on_mac.tooltip"))
                        .setEnabled(MinecraftClient.IS_SYSTEM_MAC)
                        .setImpact(OptionImpact.HIGH)
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.reduceResolutionOnMac = value, opts -> opts.extraSettings.reduceResolutionOnMac)
                        .build()
                ).build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(SodiumExtraGameOptions.OverlayCorner.class, sodiumExtraOpts)
                        .setName(Text.translatable("sodium-extra.option.overlay_corner"))
                        .setTooltip(Text.translatable("sodium-extra.option.overlay_corner.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumExtraGameOptions.OverlayCorner.class))
                        .setBinding((opts, value) -> opts.extraSettings.overlayCorner = value, opts -> opts.extraSettings.overlayCorner)
                        .build()
                )
                .add(OptionImpl.createBuilder(SodiumExtraGameOptions.TextContrast.class, sodiumExtraOpts)
                        .setName(Text.translatable("sodium-extra.option.text_contrast"))
                        .setTooltip(Text.translatable("sodium-extra.option.text_contrast.tooltip"))
                        .setControl(option -> new CyclingControl<>(option, SodiumExtraGameOptions.TextContrast.class))
                        .setBinding((opts, value) -> opts.extraSettings.textContrast = value, opts -> opts.extraSettings.textContrast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setName(Text.translatable("sodium-extra.option.show_fps"))
                        .setTooltip(Text.translatable("sodium-extra.option.show_fps.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showFps = value, opts -> opts.extraSettings.showFps)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setName(Text.translatable("sodium-extra.option.show_fps_extended"))
                        .setTooltip(Text.translatable("sodium-extra.option.show_fps_extended.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showFPSExtended = value, opts -> opts.extraSettings.showFPSExtended)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setName(Text.translatable("sodium-extra.option.show_coordinates"))
                        .setTooltip(Text.translatable("sodium-extra.option.show_coordinates.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((opts, value) -> opts.extraSettings.showCoords = value, opts -> opts.extraSettings.showCoords)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.cloud").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.cloud_height"))
                        .setTooltip(Text.translatable("sodium-extra.option.cloud_height.tooltip"))
                        .setControl(option -> new SliderControl(option, -64, 319, 1, ControlValueFormatter.number()))
                        .setBinding((options, value) -> options.extraSettings.cloudHeight = value, options -> options.extraSettings.cloudHeight)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sodium.cloud").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.cloud_distance"))
                        .setTooltip(Text.translatable("sodium-extra.option.cloud_distance.tooltip"))
                        .setControl(option -> new SliderControl(option, 100, 300, 10, ControlValueFormatter.percentage()))
                        .setBinding((options, value) -> options.extraSettings.cloudDistance = value, options -> options.extraSettings.cloudDistance)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                        .add(OptionImpl.createBuilder(boolean.class, vanillaOpts)
                                .setName(Text.translatable("sodium-extra.option.advanced_item_tooltips"))
                                .setTooltip(Text.translatable("sodium-extra.option.advanced_item_tooltips.tooltip"))
                                .setControl(TickBoxControl::new)
                                .setBinding((opts, value) -> opts.advancedItemTooltips = value, opts -> opts.advancedItemTooltips)
                                .build()
                        )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.toasts"))
                        .setTooltip(Text.translatable("sodium-extra.option.toasts.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.toasts = value, options -> options.extraSettings.toasts)
                        .build())
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.advancement_toast"))
                        .setTooltip(Text.translatable("sodium-extra.option.advancement_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.advancementToast = value, options -> options.extraSettings.advancementToast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.recipe_toast"))
                        .setTooltip(Text.translatable("sodium-extra.option.recipe_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.recipeToast = value, options -> options.extraSettings.recipeToast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.system_toast"))
                        .setTooltip(Text.translatable("sodium-extra.option.system_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.systemToast = value, options -> options.extraSettings.systemToast)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.tutorial_toast"))
                        .setTooltip(Text.translatable("sodium-extra.option.tutorial_toast.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.tutorialToast = value, options -> options.extraSettings.tutorialToast)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.instant_sneak").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.instant_sneak"))
                        .setTooltip(Text.translatable("sodium-extra.option.instant_sneak.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.instantSneak = value, options -> options.extraSettings.instantSneak)
                        .build()
                )
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.prevent_shaders").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.prevent_shaders"))
                        .setTooltip(Text.translatable("sodium-extra.option.prevent_shaders.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.preventShaders = value, options -> options.extraSettings.preventShaders)
                        .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                        .build()
                )
                .build());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(boolean.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.steady_debug_hud"))
                        .setTooltip(Text.translatable("sodium-extra.option.steady_debug_hud.tooltip"))
                        .setControl(TickBoxControl::new)
                        .setBinding((options, value) -> options.extraSettings.steadyDebugHud = value, options -> options.extraSettings.steadyDebugHud)
                        .build()
                )
                .add(OptionImpl.createBuilder(int.class, sodiumExtraOpts)
                        .setEnabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                        .setName(Text.translatable("sodium-extra.option.steady_debug_hud_refresh_interval"))
                        .setTooltip(Text.translatable("sodium-extra.option.steady_debug_hud_refresh_interval.tooltip"))
                        .setControl(option -> new SliderControlExtended(option, 1, 20, 1, ControlValueFormatterExtended.ticks(), false))
                        .setBinding((options, value) -> options.extraSettings.steadyDebugHudRefreshInterval = value, options -> options.extraSettings.steadyDebugHudRefreshInterval)
                        .build()
                )
                .build());

        return new OptionPage(Text.translatable("sodium-extra.option.extras"), ImmutableList.copyOf(groups));
    }

    private static Text translatableName(Identifier identifier, String category) {
        String key = identifier.toTranslationKey("options.".concat(category));

        Text translatable = Text.translatable(key);
        if (!Texts.hasTranslation(translatable)) {
            translatable = Text.literal(
                    Arrays.stream(key.substring(key.lastIndexOf('.') + 1).split("_"))
                            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                            .collect(Collectors.joining(" "))
            );
        }
        return translatable;
    }

    private static Text translatableTooltip(Identifier identifier, String category) {
        String key = identifier.toTranslationKey("options.".concat(category)).concat(".tooltip");

        Text translatable = Text.translatable(key);
        if (!Texts.hasTranslation(translatable)) {
            translatable = Text.translatable(
                    "sodium-extra.option.".concat(category).concat(".tooltips"),
                    translatableName(identifier, category)
            );
        }
        return translatable;
    }
}
