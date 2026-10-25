#version 330

#moj_import <sodium-extra:panini.glsl>

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform PaniniConfig {
    vec4 Params;
};

in vec2 texCoord;

out vec4 fragColor;

vec4 samplePanini(vec2 sourceUv) {
    vec2 sourceSize = vec2(textureSize(InSampler, 0));
    // Measure the warped pixel footprint before clamping at the image edges.
    vec2 sourceStepX = dFdx(sourceUv);
    vec2 sourceStepY = dFdy(sourceUv);
    vec2 axisFootprint = vec2(length(sourceStepX * sourceSize), length(sourceStepY * sourceSize));
    vec2 filterScale = smoothstep(vec2(1.0), vec2(1.25), axisFootprint);
    vec2 halfTexel = 0.5 / sourceSize;
    sourceUv = clamp(sourceUv, halfTexel, 1.0 - halfTexel);
    if (all(equal(filterScale, vec2(0.0)))) {
        return textureLod(InSampler, sourceUv, 0.0);
    }

    // A fixed 4x4 grid bounds the cost of downsampling. Collapse each axis
    // smoothly to bilinear sampling where the projection stops reducing it.
    sourceStepX *= filterScale.x;
    sourceStepY *= filterScale.y;
    vec4 color = vec4(0.0);
    for (int y = 0; y < 4; y++) {
        for (int x = 0; x < 4; x++) {
            vec2 offset = (vec2(x, y) + 0.5) / 4.0 - 0.5;
            vec2 sampleUv = clamp(sourceUv + sourceStepX * offset.x + sourceStepY * offset.y, halfTexel, 1.0 - halfTexel);
            color += textureLod(InSampler, sampleUv, 0.0);
        }
    }
    // Filter coverage and RGB together to preserve the raw outline mask.
    return color / 16.0;
}

vec4 restoreOutlineMaskColor(vec4 color) {
    if (Params.w > 0.5) {
        // The raw outline mask is filtered with coverage-weighted RGB. Vanilla
        // Sobel multiplies by alpha itself, so give that pass straight RGB.
        if (color.a > 0.0) {
            color.rgb /= color.a;
        } else {
            color.rgb = vec3(0.0);
        }
    }
    return color;
}

void main() {
    fragColor = samplePanini(paniniSourceUv(texCoord, Params));
    fragColor = restoreOutlineMaskColor(fragColor);
}
