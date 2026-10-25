#version 150

uniform sampler2D DiffuseSampler;
uniform vec4 Params;

in vec2 texCoord;

out vec4 fragColor;

vec4 samplePanini(vec2 sourceUv) {
    vec2 sourceSize = vec2(textureSize(DiffuseSampler, 0));
    // Measure the warped pixel footprint before clamping at the image edges.
    vec2 sourceStepX = dFdx(sourceUv);
    vec2 sourceStepY = dFdy(sourceUv);
    vec2 axisFootprint = vec2(length(sourceStepX * sourceSize), length(sourceStepY * sourceSize));
    vec2 filterScale = smoothstep(vec2(1.0), vec2(1.25), axisFootprint);
    vec2 halfTexel = 0.5 / sourceSize;
    sourceUv = clamp(sourceUv, halfTexel, 1.0 - halfTexel);
    if (all(equal(filterScale, vec2(0.0)))) {
        return textureLod(DiffuseSampler, sourceUv, 0.0);
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
            color += textureLod(DiffuseSampler, sampleUv, 0.0);
        }
    }
    // Filter coverage and RGB together to preserve the raw outline mask.
    return color / 16.0;
}

vec4 restoreOutlineMaskColor(vec4 color) {
    if (Params.w > 0.5) {
        // Vanilla Sobel applies alpha itself, so restore straight RGB after
        // filtering the raw mask's coverage-weighted color.
        if (color.a > 0.0) {
            color.rgb /= color.a;
        } else {
            color.rgb = vec3(0.0);
        }
    }
    return color;
}

void main() {
    float paniniD = clamp(Params.x, 0.0, 1.0);
    vec2 sourceExtent = max(Params.yz, vec2(0.0001));

    // Scale the Panini lens from the actual perspective projection so the
    // horizontal FOV remains stable across FOV effects and aspect ratios.
    float cosLonEdge = inversesqrt(1.0 + sourceExtent.x * sourceExtent.x);
    float edgeScale = (paniniD + 1.0) / (paniniD + cosLonEdge);
    float fitScale = max(edgeScale * cosLonEdge, 0.0001);

    vec2 ndc = texCoord * 2.0 - 1.0;
    vec2 paniniPlane = ndc * sourceExtent * fitScale;

    float dPlusOne = paniniD + 1.0;
    float normalizedPaniniX = paniniPlane.x / dPlusOne;
    float normalizedPaniniX2 = normalizedPaniniX * normalizedPaniniX;
    float discriminant = 1.0 + normalizedPaniniX2 * (1.0 - paniniD * paniniD);
    float cosLon = (-normalizedPaniniX2 * paniniD + sqrt(max(0.0, discriminant))) / (normalizedPaniniX2 + 1.0);
    float paniniScale = dPlusOne / (paniniD + cosLon);
    float projectionDivisor = max(paniniScale * cosLon, 0.0001);

    vec2 sourceNdc = paniniPlane / (projectionDivisor * sourceExtent);
    fragColor = samplePanini(sourceNdc * 0.5 + 0.5);
    fragColor = restoreOutlineMaskColor(fragColor);
}
