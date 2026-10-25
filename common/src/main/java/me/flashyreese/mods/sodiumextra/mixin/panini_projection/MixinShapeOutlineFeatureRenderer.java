package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.feature.ShapeOutlineFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapeOutlineFeatureRenderer.class)
public class MixinShapeOutlineFeatureRenderer {
    @ModifyExpressionValue(method = "buildGroup", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/ShapeOutlineFeatureRenderer$Submit;width()F"))
    private float sodiumExtra$scaleWorldOutlineWidth(float width) {
        return width * ((PaniniProjectionAccess) Minecraft.getInstance().gameRenderer).sodiumExtra$getPaniniProjection().worldScale();
    }
}
