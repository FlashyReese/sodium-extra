package me.flashyreese.mods.sodiumextra.client.services;

import com.mojang.blaze3d.pipeline.RenderTarget;

public class NeoForgeRenderTargetFactory implements RenderTargetFactory {
    @Override
    public RenderTarget create(RenderTarget template) {
        RenderTarget target = new RenderTarget(template.useDepth) {};
        // enableStencil() immediately resizes its target in 1.21.1. Copy the
        // flag before allocation so failures can be cleaned up by the caller.
        target.stencilEnabled = template.isStencilEnabled();
        return target;
    }

    @Override
    public boolean matches(RenderTarget target, RenderTarget template) {
        return target.useDepth == template.useDepth && target.isStencilEnabled() == template.isStencilEnabled();
    }
}
