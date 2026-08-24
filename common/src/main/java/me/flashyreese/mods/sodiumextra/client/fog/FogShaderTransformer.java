package me.flashyreese.mods.sodiumextra.client.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Patches Sodium's terrain shader so render-distance fog can use cylindrical, radial, or planar distance.
 * Non-vanilla shapes are encoded as offset bands in the existing fog UBO and decoded per frame, so toggling
 * the shape does not require a shader reload. If Sodium changes an expected shader anchor, the transform
 * latches unsupported and the Java side stops encoding offsets.
 */
public final class FogShaderTransformer {
    private static final String TOTAL_FOG_MARKER = "sodium_extra_total_fog_value";
    private static final String PLANAR_VARYING_MARKER = "v_PlanarDistance";
    private static final String CYLINDRICAL_VARYING_MARKER = "v_SodiumExtraCylindricalDistance";
    private static final String TOTAL_FOG_DECL = "float total_fog_value(";

    private static final String TOTAL_FOG_RETURN =
            "return max(linear_fog_value(sphericalVertexDistance, environmentalStart, environmantalEnd), linear_fog_value(cylindricalVertexDistance, renderDistanceStart, renderDistanceEnd));";

    private static final String TOTAL_FOG_CALL =
            "return sodium_extra_total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, environmentalStart, environmantalEnd, renderDistanceStart, renderDistanceEnd);";

    private static final String VERTEX_DECL_ANCHOR = "out vec2 v_TexCoord;";
    private static final String VERTEX_COMPUTE_ANCHOR =
            "gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);";

    private static final String FRAGMENT_DECL_ANCHOR = "in vec2 v_TexCoord;";
    private static final String FRAGMENT_FOG_CALL_ANCHOR = "fragColor = _linearFog(";

    // Keep these offsets in sync with FogDistanceHelper; they are encoded into FogData.renderDistanceStart/End.
    private static final String SHAPE_HELPER = """
            const float SODIUM_EXTRA_RADIAL_FOG_OFFSET = 1048576.0;
            const float SODIUM_EXTRA_PLANAR_FOG_OFFSET = 2097152.0;
            const float SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET = 3145728.0;
            const float SODIUM_EXTRA_CYLINDRICAL_VERTICAL_SCALE = %s;

            float sodiumExtra_planarDistance = 0.0;
            vec2 sodiumExtra_cylindricalDistance = vec2(0.0);

            float sodium_extra_cylindrical_fog_value(float horizontalDistance, float verticalDistance, float fogStart, float fogEnd) {
                float scaledDistance = max(horizontalDistance, verticalDistance / SODIUM_EXTRA_CYLINDRICAL_VERTICAL_SCALE);
                return linear_fog_value(scaledDistance, fogStart, fogEnd);
            }

            float sodium_extra_total_fog_value(float sphericalVertexDistance, float cylindricalVertexDistance, float environmentalStart, float environmentalEnd, float renderDistanceStart, float renderDistanceEnd) {
                if (renderDistanceStart >= SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET && renderDistanceEnd >= SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET) {
                    float decodedRenderDistanceStart = renderDistanceStart - SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET;
                    float decodedRenderDistanceEnd = renderDistanceEnd - SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET;
                    float horizontalDistance = sodiumExtra_cylindricalDistance.x;
                    float verticalDistance = sodiumExtra_cylindricalDistance.y;
                    float environmentalFog = sodium_extra_cylindrical_fog_value(horizontalDistance, verticalDistance, environmentalStart, environmentalEnd);
                    float renderDistanceFog = sodium_extra_cylindrical_fog_value(horizontalDistance, verticalDistance, decodedRenderDistanceStart, decodedRenderDistanceEnd);
                    return max(environmentalFog, renderDistanceFog);
                }

                if (renderDistanceStart >= SODIUM_EXTRA_PLANAR_FOG_OFFSET && renderDistanceEnd >= SODIUM_EXTRA_PLANAR_FOG_OFFSET) {
                    return max(linear_fog_value(sphericalVertexDistance, environmentalStart, environmentalEnd), linear_fog_value(sodiumExtra_planarDistance, renderDistanceStart - SODIUM_EXTRA_PLANAR_FOG_OFFSET, renderDistanceEnd - SODIUM_EXTRA_PLANAR_FOG_OFFSET));
                }

                if (renderDistanceStart >= SODIUM_EXTRA_RADIAL_FOG_OFFSET && renderDistanceEnd >= SODIUM_EXTRA_RADIAL_FOG_OFFSET) {
                    return max(linear_fog_value(sphericalVertexDistance, environmentalStart, environmentalEnd), linear_fog_value(sphericalVertexDistance, renderDistanceStart - SODIUM_EXTRA_RADIAL_FOG_OFFSET, renderDistanceEnd - SODIUM_EXTRA_RADIAL_FOG_OFFSET));
                }

                return max(linear_fog_value(sphericalVertexDistance, environmentalStart, environmentalEnd), linear_fog_value(cylindricalVertexDistance, renderDistanceStart, renderDistanceEnd));
            }

            """.formatted(Float.toString(FogDistanceHelper.CYLINDRICAL_VERTICAL_SCALE));

    private static final String VERTEX_PLANAR_DECL = "\nout float v_PlanarDistance;";
    private static final String VERTEX_PLANAR_COMPUTE = "v_PlanarDistance = abs((u_ModelViewMatrix * vec4(position, 1.0)).z);\n\n    ";
    private static final String VERTEX_CYLINDRICAL_DECL = "\nout vec2 v_SodiumExtraCylindricalDistance;";
    private static final String VERTEX_CYLINDRICAL_COMPUTE = "v_SodiumExtraCylindricalDistance = vec2(length(position.xz), abs(position.y));\n    ";
    private static final String FRAGMENT_PLANAR_DECL = "\nin float v_PlanarDistance;";
    private static final String FRAGMENT_PLANAR_ASSIGN = "sodiumExtra_planarDistance = v_PlanarDistance;\n    ";
    private static final String FRAGMENT_CYLINDRICAL_DECL = "\nin vec2 v_SodiumExtraCylindricalDistance;";
    private static final String FRAGMENT_CYLINDRICAL_ASSIGN = "sodiumExtra_cylindricalDistance = v_SodiumExtraCylindricalDistance;\n    ";

    private static final AtomicBoolean WARNED = new AtomicBoolean(false);

    // Latched false once Sodium's terrain shader no longer contains the expected anchors.
    private static volatile boolean shapeSupported = true;

    public static boolean isShapeSupported() {
        return shapeSupported;
    }

    public static String injectRenderDistanceShape(String source) {
        if (source == null || !source.contains(TOTAL_FOG_DECL)) {
            return source;
        }

        String result = source;

        if (!result.contains(TOTAL_FOG_MARKER)) {
            if (!result.contains(TOTAL_FOG_RETURN)) {
                warnDrift();
                return source;
            }

            // Replace before injecting the helper; the helper contains the same fallback expression.
            result = result
                    .replace(TOTAL_FOG_RETURN, TOTAL_FOG_CALL)
                    .replace(TOTAL_FOG_DECL, SHAPE_HELPER + TOTAL_FOG_DECL);
        }

        boolean needsPlanarVarying = !result.contains(PLANAR_VARYING_MARKER);
        boolean needsCylindricalVarying = !result.contains(CYLINDRICAL_VARYING_MARKER);
        if (needsPlanarVarying || needsCylindricalVarying) {
            boolean isVertexShader = result.contains(VERTEX_DECL_ANCHOR) && result.contains(VERTEX_COMPUTE_ANCHOR);
            boolean isFragmentShader = result.contains(FRAGMENT_DECL_ANCHOR) && result.contains(FRAGMENT_FOG_CALL_ANCHOR);

            if (isVertexShader) {
                String declarations = "";
                String computations = "";

                if (needsPlanarVarying) {
                    declarations += VERTEX_PLANAR_DECL;
                    computations += VERTEX_PLANAR_COMPUTE;
                }

                if (needsCylindricalVarying) {
                    declarations += VERTEX_CYLINDRICAL_DECL;
                    computations += VERTEX_CYLINDRICAL_COMPUTE;
                }

                result = result
                        .replace(VERTEX_DECL_ANCHOR, VERTEX_DECL_ANCHOR + declarations)
                        .replace(VERTEX_COMPUTE_ANCHOR, computations + VERTEX_COMPUTE_ANCHOR);
            } else if (isFragmentShader) {
                String declarations = "";
                String assignments = "";

                if (needsPlanarVarying) {
                    declarations += FRAGMENT_PLANAR_DECL;
                    assignments += FRAGMENT_PLANAR_ASSIGN;
                }

                if (needsCylindricalVarying) {
                    declarations += FRAGMENT_CYLINDRICAL_DECL;
                    assignments += FRAGMENT_CYLINDRICAL_ASSIGN;
                }

                result = result
                        .replace(FRAGMENT_DECL_ANCHOR, FRAGMENT_DECL_ANCHOR + declarations)
                        .replace(FRAGMENT_FOG_CALL_ANCHOR, assignments + FRAGMENT_FOG_CALL_ANCHOR);
            } else {
                warnDrift();
            }
        }

        return result;
    }

    private static void warnDrift() {
        shapeSupported = false;
        if (WARNED.compareAndSet(false, true)) {
            SodiumExtraClientMod.logger().warn(
                    "Sodium's terrain fog shader no longer matches the expected layout; custom fog shapes are partly disabled. The fog shader patch needs to be re-synced with this version.");
        }
    }
}
