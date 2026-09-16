#version 330

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
    vec2 sourceUv = clamp(sourceNdc * 0.5 + 0.5, 0.0, 1.0);

    fragColor = texture(InSampler, sourceUv);
}
