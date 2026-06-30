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
    float aspect = max(Params.y, 0.0001);

    float cosLonEdge = 1.0 / sqrt(1.0 + aspect * aspect);
    float edgeScale = (paniniD + 1.0) / (paniniD + cosLonEdge);
    float fitZoom = 1.0 / max(edgeScale * cosLonEdge, 0.0001);

    vec2 ndc = texCoord * 2.0 - 1.0;
    vec2 paniniPlane = vec2(ndc.x * aspect, ndc.y) / fitZoom;

    float dPlusOne = paniniD + 1.0;
    float normalizedPaniniX = paniniPlane.x / dPlusOne;
    float normalizedPaniniX2 = normalizedPaniniX * normalizedPaniniX;
    float discriminant = 1.0 + normalizedPaniniX2 * (1.0 - paniniD * paniniD);
    float cosLon = (-normalizedPaniniX2 * paniniD + sqrt(max(0.0, discriminant))) / (normalizedPaniniX2 + 1.0);
    float paniniScale = dPlusOne / (paniniD + cosLon);
    float projectionDivisor = max(paniniScale * cosLon, 0.0001);

    vec2 sourceNdc = vec2((paniniPlane.x / projectionDivisor) / aspect, paniniPlane.y / projectionDivisor);
    vec2 sourceUv = sourceNdc * 0.5 + 0.5;
    ivec2 sourceSize = textureSize(InSampler, 0);
    ivec2 sourceTexel = ivec2(clamp(sourceUv, 0.0, 1.0) * vec2(sourceSize - 1));

    fragColor = texelFetch(InSampler, sourceTexel, 0);
}
