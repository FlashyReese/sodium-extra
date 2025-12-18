package me.flashyreese.mods.sodiumextra.mixin.cloud;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
    @Redirect(method = {"method_62205", "lambda$addCloudsPass$3"}, require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/CloudRenderer;render(ILnet/minecraft/client/CloudStatus;FLnet/minecraft/world/phys/Vec3;JF)V"))
    private void modifyCloudHeight(CloudRenderer instance, int i, CloudStatus cloudStatus, float f, Vec3 vec3, long l, float g) {
        // todo: don't force overwrite
        instance.render(i, cloudStatus, SodiumExtraClientMod.options().extraSettings.cloudHeight, vec3, l, g);
    }
}
