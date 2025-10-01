package me.flashyreese.mods.sodiumextra.mixin.cloud;

import me.flashyreese.mods.sodiumextra.client.DimensionCloudHeightManager;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DimensionType.class)
public class MixinDimensionType {
    @Inject(method = "cloudHeight", at = @At(value = "TAIL"), cancellable = true)
    public void cloudHeight(CallbackInfoReturnable<Optional<Integer>> cir) {

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        Holder<DimensionType> holder = client.level.dimensionTypeRegistration();
        ResourceKey<DimensionType> typeKey = holder.unwrapKey().orElse(null);
        if (typeKey != null) {
            Optional<Integer> override = DimensionCloudHeightManager.getInstance().getHeight(typeKey);
            //override.ifPresent(height -> cir.setReturnValue(Optional.of(height)));
            // todo:
        }

        if (cir.getReturnValue().isPresent()) {
            cir.setReturnValue(Optional.of(SodiumExtraClientMod.options().extraSettings.cloudHeight));
        }
    }
}
