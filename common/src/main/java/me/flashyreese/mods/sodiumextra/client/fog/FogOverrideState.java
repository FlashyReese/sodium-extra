package me.flashyreese.mods.sodiumextra.client.fog;

public final class FogOverrideState {
    private static boolean settingUpCloudFog;

    private FogOverrideState() {
    }

    public static boolean isSettingUpCloudFog() {
        return settingUpCloudFog;
    }

    public static void whileSettingUpCloudFog(Runnable runnable) {
        boolean previous = settingUpCloudFog;
        settingUpCloudFog = true;
        try {
            runnable.run();
        } finally {
            settingUpCloudFog = previous;
        }
    }
}
