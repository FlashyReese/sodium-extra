package me.flashyreese.mods.sodiumextra.client.services;

import com.mojang.blaze3d.pipeline.RenderTarget;

import java.util.ServiceLoader;

public interface RenderTargetFactory {
    RenderTargetFactory INSTANCE = ServiceLoader.load(RenderTargetFactory.class, RenderTargetFactory.class.getClassLoader())
            .findFirst().orElseThrow(() -> new IllegalStateException("No render target factory is available"));

    // Copy attachment settings without allocating GPU buffers; the caller owns allocation and cleanup.
    RenderTarget create(String label, RenderTarget template);
}
