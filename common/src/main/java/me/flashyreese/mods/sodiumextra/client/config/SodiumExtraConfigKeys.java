package me.flashyreese.mods.sodiumextra.client.config;

/**
 * Shared filename and serialized-property names for the Sodium Extra config.
 * <p>
 * These are referenced both by {@link SodiumExtraGameOptions} (via {@code @SerializedName}, which pins
 * the JSON keys regardless of field renames or the Gson naming policy) and by the early-startup
 * {@code WaylandFullscreenResolutionRecovery}, which reads the config file directly before the normal
 * load path runs. Keeping them here keeps both sides in lock-step.
 */
public final class SodiumExtraConfigKeys {
    public static final String FILE_NAME = "sodium-extra-options.json";
    public static final String EXTRA_SETTINGS = "extra_settings";
    public static final String WAYLAND_FULLSCREEN_RESOLUTION = "wayland_fullscreen_resolution";
    public static final String WAYLAND_FULLSCREEN_RESOLUTION_RECOVERY_PENDING = "wayland_fullscreen_resolution_recovery_pending";
}
