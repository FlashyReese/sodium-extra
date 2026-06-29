package me.flashyreese.mods.sodiumextra.client.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.mixin.fog.AccessorMinecraftServer;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.option.Range;
import net.caffeinemc.mods.sodium.api.config.option.SteppedValidator;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.Config;
import net.caffeinemc.mods.sodium.client.config.structure.IntegerOption;
import net.caffeinemc.mods.sodium.client.config.structure.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.players.PlayerList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class FogDistanceHelper {
    public static final Identifier SODIUM_RENDER_DISTANCE_OPTION_ID = Identifier.parse("sodium:general.render_distance");
    public static final int FOG_DISTANCE_OFF = -1;
    public static final int FOG_DISTANCE_VANILLA = 0;
    private static final int LEGACY_FOG_DISTANCE_OFF = 33;
    private static final int VANILLA_MAX_FOG_DISTANCE = 32;
    private static final int PROTECTED_FOG_DISTANCE_MAX_BLOCKS = 256;
    // Shape sentinels decoded by FogShaderTransformer; keep values in sync with its GLSL constants.
    private static final float RADIAL_RENDER_DISTANCE_OFFSET = 1_048_576.0F;
    private static final float PLANAR_RENDER_DISTANCE_OFFSET = 2_097_152.0F;
    private static final float CHUNK_SIZE = 16F;

    public enum ProtectedFogType {
        BLINDNESS,
        DARKNESS,
        LAVA,
        POWDER_SNOW,
        WATER
    }

    public static SodiumExtraGameOptions.AtmosphericFogSettings getAtmosphericSettings(ClientLevel level) {
        SodiumExtraGameOptions.FogSettings fogSettings = getFogSettings();
        Identifier dimensionEffectsId = level.dimensionTypeRegistration()
                .unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(() -> level.dimension().identifier());
        return fogSettings.getAtmospheric(dimensionEffectsId);
    }

    public static int getFogDistance(ClientLevel level) {
        return getAtmosphericSettings(level).distanceChunks;
    }

    public static int normalizeFogDistance(int fogDistance) {
        return fogDistance == LEGACY_FOG_DISTANCE_OFF ? FOG_DISTANCE_OFF : fogDistance;
    }

    public static Range getFogDistanceRange(ConfigState state) {
        return new Range(FOG_DISTANCE_OFF, getMaxFogDistance(state), 1);
    }

    public static Range getProtectedGameplayFogDistanceRange() {
        return new Range(FOG_DISTANCE_OFF, PROTECTED_FOG_DISTANCE_MAX_BLOCKS, 1);
    }

    public static int getMaxFogDistance() {
        return getMaxFogDistance(null);
    }

    public static int getMaxFogDistance(ConfigState state) {
        int maxFogDistance = VANILLA_MAX_FOG_DISTANCE;
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft != null && minecraft.options != null) {
            maxFogDistance = Math.max(maxFogDistance, minecraft.options.renderDistance().get());
            Object valueSet = minecraft.options.renderDistance().values();

            if (valueSet instanceof OptionInstance.IntRange range) {
                maxFogDistance = Math.max(maxFogDistance, range.maxInclusive());
            } else if (valueSet instanceof OptionInstance.ClampingLazyMaxIntRange range) {
                maxFogDistance = Math.max(maxFogDistance, range.maxInclusive());
            } else {
                maxFogDistance = Math.max(maxFogDistance, getIntAccessor(valueSet, "maxInclusive", maxFogDistance));
            }
        }

        maxFogDistance = Math.max(maxFogDistance, getSodiumRenderDistanceMax(state, maxFogDistance));

        SodiumExtraGameOptions.FogSettings fogSettings = getFogSettings();
        maxFogDistance = Math.max(maxFogDistance, normalizeFogDistance(fogSettings.atmospheric.distanceChunks));
        for (SodiumExtraGameOptions.AtmosphericFogSettings settings : fogSettings.dimensionOverrides.values()) {
            maxFogDistance = Math.max(maxFogDistance, normalizeFogDistance(settings.distanceChunks));
        }

        return maxFogDistance;
    }

    private static int getSodiumRenderDistanceMax(ConfigState state, int fallback) {
        Config config = getSodiumConfig(state);
        if (config == null) {
            return fallback;
        }

        try {
            Option option = config.getOption(SODIUM_RENDER_DISTANCE_OPTION_ID);
            if (option instanceof IntegerOption integerOption) {
                SteppedValidator validator = integerOption.getSteppedValidator();
                return validator.max();
            }
        } catch (RuntimeException ignored) {
        }

        return fallback;
    }

    private static Config getSodiumConfig(ConfigState state) {
        if (state instanceof Config config) {
            return config;
        }

        Config reflectedConfig = getConfigFromState(state);
        return reflectedConfig != null ? reflectedConfig : ConfigManager.CONFIG;
    }

    private static Config getConfigFromState(ConfigState state) {
        if (state == null) {
            return null;
        }

        Class<?> type = state.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("state");
                field.setAccessible(true);
                Object value = field.get(state);
                return value instanceof Config config ? config : null;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return null;
            }
        }

        return null;
    }

    public static float getStart(SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        return settings.distanceChunks * CHUNK_SIZE * (settings.startPercent / 100.0F);
    }

    public static float applyStartMultiplier(float start, SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        return start * (settings.startPercent / 100.0F);
    }

    public static float getEnd(int fogDistance) {
        return (fogDistance + 1) * CHUNK_SIZE;
    }

    public static boolean disablesFog(int fogDistance) {
        return fogDistance == FOG_DISTANCE_OFF;
    }

    public static void applyRenderDistanceShape(FogData fog, SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        // VANILLA/CYLINDRICAL use the unmodified shader path. If the transformer failed, skip offsets too.
        if (fog.renderDistanceEnd == Float.MAX_VALUE || !FogShaderTransformer.isShapeSupported()) {
            return;
        }

        float offset = switch (settings.shapeMode) {
            case RADIAL -> RADIAL_RENDER_DISTANCE_OFFSET;
            case PLANAR -> PLANAR_RENDER_DISTANCE_OFFSET;
            default -> 0.0F;
        };

        if (offset != 0.0F) {
            fog.renderDistanceStart += offset;
            fog.renderDistanceEnd += offset;
        }
    }

    public static boolean isBossFogActive() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.gui != null && minecraft.gui.getBossOverlay().shouldCreateWorldFog();
    }

    public static boolean shouldModifyProtectedGameplayFog() {
        SodiumExtraGameOptions.FogSettings fogSettings = getFogSettings();
        return fogSettings.advanced && fogSettings.protectedGameplay.enabledWhenAllowed && isLocalWorldAllowedForProtectedGameplayFog();
    }

    public static int getProtectedGameplayFogDistance(ProtectedFogType type) {
        SodiumExtraGameOptions.ProtectedFogSettings settings = getFogSettings().protectedGameplay;
        return switch (type) {
            case BLINDNESS -> settings.blindnessDistanceBlocks;
            case DARKNESS -> settings.darknessDistanceBlocks;
            case LAVA -> settings.lavaDistanceBlocks;
            case POWDER_SNOW -> settings.powderSnowDistanceBlocks;
            case WATER -> settings.waterDistanceBlocks;
        };
    }

    public static void applyProtectedGameplayFog(FogData fog, int distanceBlocks, float environmentalStartMultiplier, float skyEndMultiplier) {
        distanceBlocks = normalizeFogDistance(distanceBlocks);
        if (distanceBlocks == FOG_DISTANCE_VANILLA) {
            return;
        }

        if (disablesFog(distanceBlocks)) {
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
            fog.skyEnd = Float.MAX_VALUE;
            fog.cloudEnd = Float.MAX_VALUE;
            return;
        }

        float end = distanceBlocks;
        fog.environmentalStart = end * environmentalStartMultiplier;
        fog.environmentalEnd = end;
        fog.skyEnd = end * skyEndMultiplier;
        fog.cloudEnd = end * skyEndMultiplier;
    }

    private static SodiumExtraGameOptions.FogSettings getFogSettings() {
        SodiumExtraGameOptions.RenderSettings renderSettings = SodiumExtraClientMod.options().renderSettings;
        renderSettings.sanitize();
        return renderSettings.fogSettings;
    }

    private static boolean isLocalWorldAllowedForProtectedGameplayFog() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || !minecraft.hasSingleplayerServer()) {
            return false;
        }

        if (minecraft.isSingleplayer()) {
            return true;
        }

        Object server = minecraft.getSingleplayerServer();
        PlayerList playerList = server instanceof AccessorMinecraftServer accessor ? accessor.sodiumExtra$getPlayerList() : null;
        return playerList != null && playerList.isAllowCommandsForAllPlayers();
    }

    private static int getIntAccessor(Object object, String methodName, int fallback) {
        if (object == null) {
            return fallback;
        }

        try {
            Method method = object.getClass().getMethod(methodName);
            Object result = method.invoke(object);
            return result instanceof Integer value ? value : fallback;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return fallback;
        }
    }

}
