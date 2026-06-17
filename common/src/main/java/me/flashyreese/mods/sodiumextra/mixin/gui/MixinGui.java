package me.flashyreese.mods.sodiumextra.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded, CallbackInfo ci, @Local GuiGraphicsExtractor guiGraphics) {
        if (shouldRenderLevel || resourcesLoaded) {
            SodiumExtraClientMod.onHudRender(guiGraphics, deltaTracker);
        }
    }
}
