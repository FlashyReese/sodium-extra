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
    private static final String RENDER_DISTANCE_MARKER = "render_distance_fog_value";
    private static final String PLANAR_VARYING_MARKER = "v_PlanarDistance";
    private static final String TOTAL_FOG_DECL = "float total_fog_value(";

    private static final String CYLINDRICAL_TERM =
            "linear_fog_value(cylindricalVertexDistance, renderDistanceStart, renderDistanceEnd)";

    private static final String RENDER_DISTANCE_CALL =
            "render_distance_fog_value(sphericalVertexDistance, cylindricalVertexDistance, renderDistanceStart, renderDistanceEnd)";

    private static final String VERTEX_DECL_ANCHOR = "out vec2 v_TexCoord;";
    private static final String VERTEX_COMPUTE_ANCHOR =
            "gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);";

    private static final String FRAGMENT_DECL_ANCHOR = "in vec2 v_TexCoord;";
    private static final String FRAGMENT_FOG_CALL_ANCHOR = "fragColor = _linearFog(";

    // Keep these offsets in sync with FogDistanceHelper; they are encoded into FogData.renderDistanceStart/End.
    private static final String SHAPE_HELPER = """
            const float SODIUM_EXTRA_RADIAL_FOG_OFFSET = 1048576.0;
            const float SODIUM_EXTRA_PLANAR_FOG_OFFSET = 2097152.0;

            float sodiumExtra_planarDistance = 0.0;

            float render_distance_fog_value(float sphericalVertexDistance, float cylindricalVertexDistance, float renderDistanceStart, float renderDistanceEnd) {
                if (renderDistanceStart >= SODIUM_EXTRA_PLANAR_FOG_OFFSET && renderDistanceEnd >= SODIUM_EXTRA_PLANAR_FOG_OFFSET) {
                    return linear_fog_value(sodiumExtra_planarDistance, renderDistanceStart - SODIUM_EXTRA_PLANAR_FOG_OFFSET, renderDistanceEnd - SODIUM_EXTRA_PLANAR_FOG_OFFSET);
                }

                if (renderDistanceStart >= SODIUM_EXTRA_RADIAL_FOG_OFFSET && renderDistanceEnd >= SODIUM_EXTRA_RADIAL_FOG_OFFSET) {
                    return linear_fog_value(sphericalVertexDistance, renderDistanceStart - SODIUM_EXTRA_RADIAL_FOG_OFFSET, renderDistanceEnd - SODIUM_EXTRA_RADIAL_FOG_OFFSET);
                }

                return linear_fog_value(cylindricalVertexDistance, renderDistanceStart, renderDistanceEnd);
            }

            """;

    private static final String VERTEX_PLANAR_DECL = "\nout float v_PlanarDistance;";
    private static final String VERTEX_PLANAR_COMPUTE = "v_PlanarDistance = -(u_ModelViewMatrix * vec4(position, 1.0)).z;\n\n    ";
    private static final String FRAGMENT_PLANAR_DECL = "\nin float v_PlanarDistance;";
    private static final String FRAGMENT_PLANAR_ASSIGN = "sodiumExtra_planarDistance = v_PlanarDistance;\n    ";

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

        if (!result.contains(RENDER_DISTANCE_MARKER)) {
            if (!result.contains(CYLINDRICAL_TERM)) {
                warnDrift();
                return source;
            }

            // Replace before injecting the helper; the helper contains the same fallback expression.
            result = result
                    .replace(CYLINDRICAL_TERM, RENDER_DISTANCE_CALL)
                    .replace(TOTAL_FOG_DECL, SHAPE_HELPER + TOTAL_FOG_DECL);
        }

        // Only the planar shape needs view-depth passed from vertex to fragment.
        if (!result.contains(PLANAR_VARYING_MARKER)) {
            if (result.contains(VERTEX_DECL_ANCHOR) && result.contains(VERTEX_COMPUTE_ANCHOR)) {
                result = result
                        .replace(VERTEX_DECL_ANCHOR, VERTEX_DECL_ANCHOR + VERTEX_PLANAR_DECL)
                        .replace(VERTEX_COMPUTE_ANCHOR, VERTEX_PLANAR_COMPUTE + VERTEX_COMPUTE_ANCHOR);
            } else if (result.contains(FRAGMENT_DECL_ANCHOR) && result.contains(FRAGMENT_FOG_CALL_ANCHOR)) {
                result = result
                        .replace(FRAGMENT_DECL_ANCHOR, FRAGMENT_DECL_ANCHOR + FRAGMENT_PLANAR_DECL)
                        .replace(FRAGMENT_FOG_CALL_ANCHOR, FRAGMENT_PLANAR_ASSIGN + FRAGMENT_FOG_CALL_ANCHOR);
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
