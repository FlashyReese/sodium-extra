package me.flashyreese.mods.sodiumextra.mixin.optimizations.beacon_beam_rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import me.flashyreese.mods.sodiumextra.compat.IrisCompat;
import me.flashyreese.mods.sodiumextra.compat.ModelVertexToTerrainSerializer;
import net.caffeinemc.mods.sodium.client.render.vertex.serializers.VertexSerializerRegistryImpl;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VertexSerializerRegistryImpl.class, remap = false)
public class MixinVertexSerializerRegistryImpl {

    @Shadow
    @Final
    private Long2ReferenceMap<VertexSerializer> cache;

    @Shadow
    private static long createKey(VertexFormat a, VertexFormat b) {
        return 0;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void putSerializerIris(CallbackInfo ci) {
        if (IrisCompat.isIrisPresent()) {
            this.cache.put(createKey(DefaultVertexFormat.NEW_ENTITY, IrisCompat.getTerrainFormat()), new ModelVertexToTerrainSerializer());
        }
    }
}
