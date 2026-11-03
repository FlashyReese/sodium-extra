package me.flashyreese.mods.sodiumextra.client.services;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;

import java.util.Objects;

public class ForgeRenderTargetFactory implements RenderTargetFactory {
    @Override
    public RenderTarget create(String label, RenderTarget template) {
        GpuFormat colorFormat = Objects.requireNonNull(template.getColorTexture()).getFormat();
        RenderTarget target = new RenderTarget(label, template.useDepth, colorFormat) {};
        // Forge has no stencil constructor parameter, and enableStencil() resizes (allocates) right
        // away, so the flag is copied directly; accesstransformer.cfg makes the field public.
        target.stencilEnabled = template.isStencilEnabled();
        return target;
    }
}
