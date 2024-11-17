package me.flashyreese.mods.sodiumextra.mixin.optimizations.fast_weather;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import me.flashyreese.mods.sodiumextra.client.render.vertex.formats.WeatherVertex;
import me.flashyreese.mods.sodiumextra.common.util.Utils;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1500)
public class MixinLevelRenderer {
    @Shadow
    @Final
    private static ResourceLocation RAIN_LOCATION;
    @Shadow
    @Final
    private static ResourceLocation SNOW_LOCATION;
    @Unique
    private final Long2ReferenceMap<Biome> biomeLong2ReferenceMap = new Long2ReferenceOpenHashMap<>();
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int ticks;
    @Shadow
    @Final
    private float[] rainSizeX;
    @Shadow
    @Final
    private float[] rainSizeZ;


    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    public void sodiumExtra$renderWeather(LightTexture lightTexture, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        assert this.minecraft.level != null;
        float rainGradient = this.minecraft.level.getRainLevel(tickDelta);
        if (rainGradient > 0.0F) {
            lightTexture.turnOnLightLayer();
            Level world = this.minecraft.level;
            int cameraPosX = Mth.floor(cameraX);
            int cameraPosY = Mth.floor(cameraY);
            int cameraPosZ = Mth.floor(cameraZ);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = null;
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            int range = Minecraft.useFancyGraphics() ? 10 : 5;

            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            int precipitationType = -1;
            float time = (float) this.ticks + tickDelta;
            RenderSystem.setShader(GameRenderer::getParticleShader);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int z = cameraPosZ - range; z <= cameraPosZ + range; ++z) {
                for (int x = cameraPosX - range; x <= cameraPosX + range; ++x) {
                    int positionIndex = (z - cameraPosZ + 16) * 32 + x - cameraPosX + 16;
                    double offsetX = (double) this.rainSizeX[positionIndex] * 0.5;
                    double offsetZ = (double) this.rainSizeZ[positionIndex] * 0.5;
                    mutable.set(x, cameraY, z);

                    long biomePacked = Utils.packPosition(x, z);
                    Biome biome = this.biomeLong2ReferenceMap.computeIfAbsent(biomePacked, key -> world.getBiome(mutable).value());
                    if (biome.hasPrecipitation()) {
                        int topY = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                        int minY = cameraPosY - range;
                        int maxY = cameraPosY + range;
                        if (minY < topY) {
                            minY = topY;
                        }

                        if (maxY < topY) {
                            maxY = topY;
                        }

                        int adjustedTopY = Math.max(topY, cameraPosY);

                        if (minY != maxY) {
                            RandomSource random = RandomSource.create((long) x * x * 3121 + x * 45238971L ^ (long) z * z * 418711 + z * 13761L);
                            mutable.set(x, minY, z);
                            Biome.Precipitation precipitation = biome.getPrecipitationAt(mutable);
                            if (precipitation == Biome.Precipitation.RAIN) {
                                if (precipitationType != 0) {
                                    if (precipitationType >= 0) {
                                        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
                                    }

                                    precipitationType = 0;
                                    RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                                    bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }

                                int ticksModulus = this.ticks & 131071;
                                int randomOffset = x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761 & 0xFF;
                                float dropLength = 3.0F + random.nextFloat();
                                float dropTimeOffset = -((float) (ticksModulus + randomOffset) + tickDelta) / 32.0F * dropLength;
                                float dropTextureOffset = dropTimeOffset % 32.0F;

                                double relativeX = (double) x + 0.5 - cameraX;
                                double relativeZ = (double) z + 0.5 - cameraZ;
                                float distance = (float) Math.sqrt(relativeX * relativeX + relativeZ * relativeZ) / (float) range;
                                float alpha = ((1.0F - distance * distance) * 0.5F + 0.5F) * rainGradient;
                                mutable.set(x, adjustedTopY, z);
                                int color = ColorABGR.pack(1.0F, 1.0F, 1.0F, alpha);
                                int light = LevelRenderer.getLightColor(world, mutable);

                                VertexBufferWriter writer = VertexBufferWriter.of(bufferBuilder);
                                write(writer, cameraX, cameraY, cameraZ, x, z, offsetX, offsetZ, minY, maxY, 0, dropTextureOffset, color, light);
                            } else if (precipitation == Biome.Precipitation.SNOW) {
                                if (precipitationType != 1) {
                                    if (precipitationType >= 0) {
                                        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
                                    }

                                    precipitationType = 1;
                                    RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                                    bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }

                                float snowFallSpeed = -((float) (this.ticks & 511) + tickDelta) / 512.0F;
                                float snowTextureOffsetX = (float) (random.nextDouble() + (double) time * 0.01 * (double) ((float) random.nextGaussian()));
                                float snowTextureOffsetY = (float) (random.nextDouble() + (double) (time * (float) random.nextGaussian()) * 0.001);

                                double relativeX = (double) x + 0.5 - cameraX;
                                double relativeZ = (double) z + 0.5 - cameraZ;
                                float distance = (float) Math.sqrt(relativeX * relativeX + relativeZ * relativeZ) / (float) range;
                                float alpha = ((1.0F - distance * distance) * 0.3F + 0.5F) * rainGradient;

                                mutable.set(x, adjustedTopY, z);
                                int light = LevelRenderer.getLightColor(world, mutable);
                                int blockLight = light >> 16 & (LightTexture.FULL_BLOCK | 65295);
                                int skyLight = light & (LightTexture.FULL_BLOCK | 65295);
                                int adjustedBlockLight = (blockLight * 3 + LightTexture.FULL_BLOCK) / 4;
                                int adjustedSkyLight = (skyLight * 3 + LightTexture.FULL_BLOCK) / 4;

                                VertexBufferWriter writer = VertexBufferWriter.of(bufferBuilder);
                                int color = ColorABGR.pack(1.0F, 1.0F, 1.0F, alpha);
                                int packedLight = Utils.packLight(adjustedSkyLight, adjustedBlockLight);
                                write(writer, cameraX, cameraY, cameraZ, x, z, offsetX, offsetZ, minY, maxY, snowTextureOffsetX, snowFallSpeed + snowTextureOffsetY, color, packedLight);
                            }
                        }
                    }
                }
            }

            if (precipitationType >= 0) {
                BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            lightTexture.turnOffLightLayer();
        }
        ci.cancel();
    }

    @Inject(method = "allChanged", at = @At(value = "TAIL"))
    private void postReload(CallbackInfo ci) {
        this.biomeLong2ReferenceMap.clear();
    }

    @Unique
    private void write(VertexBufferWriter writer, double cameraX, double cameraY, double cameraZ, int x, int z, double offsetX, double offsetZ, int minY, int maxY, float textureOffsetX, float textureOffsetY, int color, int light) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long buffer = stack.nmalloc(4 * WeatherVertex.STRIDE);
            long ptr = buffer;

            WeatherVertex.put(ptr,
                    (float) ((double) x - cameraX - offsetX + 0.5), (float) ((double) maxY - cameraY), (float) ((double) z - cameraZ - offsetZ + 0.5),
                    0.0F + textureOffsetX, (float) minY * 0.25F + textureOffsetY,
                    color,
                    light
            );
            ptr += WeatherVertex.STRIDE;

            WeatherVertex.put(ptr,
                    (float) ((double) x - cameraX + offsetX + 0.5), (float) ((double) maxY - cameraY), (float) ((double) z - cameraZ + offsetZ + 0.5),
                    1.0F + textureOffsetX, (float) minY * 0.25F + textureOffsetY,
                    color,
                    light
            );
            ptr += WeatherVertex.STRIDE;

            WeatherVertex.put(ptr,
                    (float) ((double) x - cameraX + offsetX + 0.5), (float) ((double) minY - cameraY), (float) ((double) z - cameraZ + offsetZ + 0.5),
                    1.0F + textureOffsetX, (float) maxY * 0.25F + textureOffsetY,
                    color,
                    light
            );
            ptr += WeatherVertex.STRIDE;

            WeatherVertex.put(ptr,
                    (float) ((double) x - cameraX - offsetX + 0.5), (float) ((double) minY - cameraY), (float) ((double) z - cameraZ - offsetZ + 0.5),
                    0.0F + textureOffsetX, (float) maxY * 0.25F + textureOffsetY,
                    color,
                    light
            );
            ptr += WeatherVertex.STRIDE;

            writer.push(stack, buffer, 4, WeatherVertex.FORMAT);
        }
    }
}
