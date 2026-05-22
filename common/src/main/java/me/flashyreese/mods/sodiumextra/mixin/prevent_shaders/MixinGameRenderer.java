package me.flashyreese.mods.sodiumextra.mixin.prevent_shaders;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "togglePostEffect", at = @At("HEAD"), cancellable = true)
    private void preventShaders(CallbackInfo ci) {
        if (SodiumExtraClientMod.options().extraSettings.preventShaders) {
            ci.cancel();
        }
    }

    @Inject(method = "loadEffect", at = @At("HEAD"), cancellable = true)
    private void dontLoadShader(ResourceLocation identifier, CallbackInfo ci) {
        if (SodiumExtraClientMod.options().extraSettings.preventShaders) {
            ci.cancel();
        }
    }
}
