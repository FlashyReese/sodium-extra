#version 330
#extension GL_ARB_separate_shader_objects : require

#include <sodium-extra:panini.glsl>

uniform sampler2D DepthSampler;

layout(std140) uniform PaniniConfig {
    vec4 Params;
};

layout(location = 0) in vec2 texCoord;

void main() {
    // Match the color projection so subsequent depth effects see the same
    // geometry, even when world and output targets have different sizes.
    vec2 sourceUv = paniniSourceUv(texCoord, Params);

    // Depth must select an actual surface rather than interpolate between them.
    ivec2 sourceSize = textureSize(DepthSampler, 0);
    ivec2 sourceTexel = clamp(ivec2(floor(sourceUv * vec2(sourceSize))), ivec2(0), sourceSize - 1);
    gl_FragDepth = texelFetch(DepthSampler, sourceTexel, 0).r;
}
