package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    @Final
    public GameRenderer gameRenderer;

    // Every world renderer looks up the main target here at draw time, so
    // redirecting it covers terrain, sky, clouds, weather and entities.
    @ModifyReturnValue(method = "getMainRenderTarget", at = @At("RETURN"))
    private RenderTarget sodiumExtra$getWorldTarget(RenderTarget nativeMain) {
        // The main target is used before the game renderer is constructed.
        if (this.gameRenderer == null) {
            return nativeMain;
        }
        return ((PaniniProjectionAccess) this.gameRenderer).sodiumExtra$getPaniniProjection().worldTarget(nativeMain);
    }
}
