package me.flashyreese.mods.sodiumextra.mixin.sodium.scrollable_page;

import me.flashyreese.mods.sodiumextra.client.gui.scrollable_page.OptionPageScrollFrame;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptionsGUI;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.prompt.ScreenPrompt;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SodiumOptionsGUI.class, remap = false)
public abstract class MixinSodiumOptionsGUI extends Screen {

    @Shadow
    private OptionPage currentPage;

    @Shadow
    private @Nullable ScreenPrompt prompt;

    protected MixinSodiumOptionsGUI(Component title) {
        super(title);
    }

    @Inject(method = "rebuildGUIOptions", at = @At(value = "HEAD"), cancellable = true)
    private void rebuildGUIOptions(CallbackInfo ci) {
        int x = 6;
        int y = 28;

        OptionPageScrollFrame optionPageScrollFrame = new OptionPageScrollFrame(new Dim2i(x, y, /*this.width - x * 2*/240, this.height - y - 10 /*- 64*/), this.currentPage);
        this.addRenderableWidget(optionPageScrollFrame);
        ci.cancel();
    }

    @Inject(method = "renderOptionTooltip", at = @At(value = "HEAD"), cancellable = true)
    private void renderOptionTooltip(GuiGraphics guiGraphics, ControlElement<?> element, CallbackInfo ci) {
        ci.cancel();
    }

    // Fixme:
    // Fixes issue introduce with Sodium 20006a85fb7a64889f507eb13521e55693ae0d7e
    // This override prevents focused element from staying focused because we are using a ParentElement for scroll frames
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean repeated) {
        if (this.prompt != null) {
            return this.prompt.mouseClicked(event, repeated);
        }
        return super.mouseClicked(event, repeated);
    }
}
