package me.flashyreese.mods.sodiumextra.client;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DimensionCloudHeightManager {
    private static final DimensionCloudHeightManager INSTANCE = new DimensionCloudHeightManager();
    private final Map<ResourceKey<DimensionType>, Integer> dimensionHeights = new HashMap<>();

    public static DimensionCloudHeightManager getInstance() {
        return DimensionCloudHeightManager.INSTANCE;
    }

    public void reload(RegistryAccess registryAccess) {
        Registry<DimensionType> registry = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE);
        this.dimensionHeights.clear();

        for (var entry : registry.entrySet()) {
            ResourceKey<DimensionType> key = entry.getKey();
            DimensionType type = entry.getValue();
            type.cloudHeight().ifPresent(height -> this.dimensionHeights.put(key, height));
        }
    }

    public Optional<Integer> getHeight(ResourceKey<DimensionType> typeKey) {
        return Optional.ofNullable(this.dimensionHeights.get(typeKey));
    }
}
