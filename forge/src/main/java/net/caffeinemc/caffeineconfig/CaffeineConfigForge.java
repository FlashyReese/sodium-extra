package net.caffeinemc.caffeineconfig;

import net.minecraftforge.fml.loading.LoadingModList;

import java.util.Map;

public class CaffeineConfigForge implements CaffeineConfigPlatform {
    @Override
    public void applyModOverrides(CaffeineConfig config, String jsonKey) {
        // Two Forge differences: LoadingModList is static, and its ModInfo is a record with no
        // getConfigElement, so overrides are read from [modproperties.<modid>] instead of a
        // top-level key of the mod entry.
        for (var meta : LoadingModList.getMods()) {
            Object override = meta.getModProperties().get(jsonKey);

            if (override == null) {
                continue;
            }

            if (override instanceof Map<?, ?> overrides && overrides.keySet().stream().allMatch(key -> key instanceof String)) {
                overrides.forEach((key, value) -> {
                    if (!(value instanceof Boolean) || !(key instanceof String)) {
                        config.getLogger().warn("Mod '{}' attempted to override option '{}' with an invalid value, ignoring", meta.getModId(), key);
                        return;
                    }

                    config.applyModOverride(meta.getModId(), (String) key, (Boolean) value);
                });
            } else {
                config.getLogger().warn("Mod '{}' contains invalid Sodium option overrides, ignoring", meta.getModId());
            }
        }
    }
}
