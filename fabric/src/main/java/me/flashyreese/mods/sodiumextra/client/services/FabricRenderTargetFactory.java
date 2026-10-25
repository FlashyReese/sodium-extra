package me.flashyreese.mods.sodiumextra.client.services;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;

import java.util.Objects;

public class FabricRenderTargetFactory implements RenderTargetFactory {
    @Override
    public RenderTarget create(String label, RenderTarget template) {
        GpuFormat colorFormat = Objects.requireNonNull(template.getColorTexture()).getFormat();
        return new RenderTarget(label, template.useDepth, colorFormat) {};
    }
}
