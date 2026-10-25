#ifndef SODIUM_EXTRA_PANINI_GLSL
#define SODIUM_EXTRA_PANINI_GLSL

// Maps an output UV to the unclamped source UV of the rectilinear world image.
// params: x = Panini distance, yz = perspective tangent half-extents.
// A distance of 0 is the identity mapping.
vec2 paniniSourceUv(vec2 uv, vec4 params) {
    float paniniD = clamp(params.x, 0.0, 1.0);
    vec2 sourceExtent = max(params.yz, vec2(0.0001));

    // Scale the Panini lens from the actual perspective projection so the
    // horizontal FOV remains stable across FOV effects and aspect ratios.
    float cosLonEdge = inversesqrt(1.0 + sourceExtent.x * sourceExtent.x);
    float edgeScale = (paniniD + 1.0) / (paniniD + cosLonEdge);
    float fitScale = max(edgeScale * cosLonEdge, 0.0001);

    vec2 ndc = uv * 2.0 - 1.0;
    vec2 paniniPlane = ndc * sourceExtent * fitScale;

    float dPlusOne = paniniD + 1.0;
    float normalizedPaniniX = paniniPlane.x / dPlusOne;
    float normalizedPaniniX2 = normalizedPaniniX * normalizedPaniniX;
    float discriminant = 1.0 + normalizedPaniniX2 * (1.0 - paniniD * paniniD);
    float cosLon = (-normalizedPaniniX2 * paniniD + sqrt(max(0.0, discriminant))) / (normalizedPaniniX2 + 1.0);
    float paniniScale = dPlusOne / (paniniD + cosLon);
    float projectionDivisor = max(paniniScale * cosLon, 0.0001);

    vec2 sourceNdc = paniniPlane / (projectionDivisor * sourceExtent);
    return sourceNdc * 0.5 + 0.5;
}

#endif
