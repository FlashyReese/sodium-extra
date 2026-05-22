package net.caffeinemc.caffeineconfig;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.util.Map;

public class CaffeineConfigNeoForge implements CaffeineConfigPlatform {
    @Override
    public void applyModOverrides(CaffeineConfig config, String jsonKey) {
        for (ModInfo meta : FMLLoader.getLoadingModList().getMods()) {
            meta.getConfigElement(jsonKey).ifPresent(override -> {
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
            });
        }
    }
}
