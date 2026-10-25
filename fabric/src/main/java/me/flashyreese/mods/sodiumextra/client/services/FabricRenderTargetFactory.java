package me.flashyreese.mods.sodiumextra.client.services;

import com.mojang.blaze3d.pipeline.RenderTarget;

public class FabricRenderTargetFactory implements RenderTargetFactory {
    @Override
    public RenderTarget create(RenderTarget template) {
        return new RenderTarget(template.useDepth) {};
    }

    @Override
    public boolean matches(RenderTarget target, RenderTarget template) {
        return target.useDepth == template.useDepth;
    }
}
