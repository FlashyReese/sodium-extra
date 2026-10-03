package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import me.flashyreese.mods.sodiumextra.client.render.EntityOutlineState;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements EntityOutlineState {
    @Unique
    private boolean sodiumExtra$hasEntityOutline;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void sodiumExtra$resetEntityOutline(CallbackInfo ci) {
        this.sodiumExtra$hasEntityOutline = false;
    }

    // The first post chain is the entity outline effect. Vanilla only runs it
    // when this frame actually contains outlines; the transparency chain is later.
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V", ordinal = 0))
    private void sodiumExtra$markEntityOutline(CallbackInfo ci) {
        this.sodiumExtra$hasEntityOutline = true;
    }

    @Override
    public boolean sodiumExtra$hasEntityOutline() {
        return this.sodiumExtra$hasEntityOutline;
    }
}
