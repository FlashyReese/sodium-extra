package me.flashyreese.mods.sodiumextra.client.services;

import com.mojang.blaze3d.pipeline.RenderTarget;

public class FabricRenderTargetFactory implements RenderTargetFactory {
    @Override
    public RenderTarget create(String label, RenderTarget template) {
        return new RenderTarget(label, template.useDepth) {};
    }
}
