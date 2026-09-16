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
    private static final String PLANAR_VARYING_MARKER = "v_PlanarDistance";
    private static final String CYLINDRICAL_VARYING_MARKER = "v_SodiumExtraCylindricalDistance";
    private static final String FOG_INCLUDE_ANCHOR = "#include <sodium:fog.glsl>";
    private static final String VERTEX_DECL_ANCHOR = "layout(location = 1) out vec2 v_TexCoord;";
    private static final String VERTEX_COMPUTE_ANCHOR =
            "gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);";

    private static final String FRAGMENT_DECL_ANCHOR = "layout(location = 1) in vec2 v_TexCoord;";
    private static final String FRAGMENT_FOG_CALL_ANCHOR = "return _linearFog(";
    private static final String FRAGMENT_FOG_CALL = "return sodium_extra_linear_fog(";

    // Keep these offsets in sync with FogDistanceHelper; they are encoded into FogData.renderDistanceStart/End.
    private static final String SHAPE_HELPER = """
            const float SODIUM_EXTRA_RADIAL_FOG_OFFSET = 1048576.0;
            const float SODIUM_EXTRA_PLANAR_FOG_OFFSET = 2097152.0;
            const float SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET = 3145728.0;
            const float SODIUM_EXTRA_FOG_SHAPE_BAND_SIZE = 1048576.0;
            const float SODIUM_EXTRA_CYLINDRICAL_VERTICAL_SCALE = %s;

            bool sodium_extra_is_shape_encoded(vec2 renderFog, float offset) {
                float bandEnd = offset + SODIUM_EXTRA_FOG_SHAPE_BAND_SIZE;
                return renderFog.x >= offset && renderFog.x < bandEnd
                    && renderFog.y >= offset && renderFog.y < bandEnd;
            }

            vec4 sodium_extra_linear_fog(vec4 fragColor, vec2 fragDistance, vec4 fogColor, vec2 environmentFog, vec2 renderFog, float fadeFactor) {
                if (sodium_extra_is_shape_encoded(renderFog, SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET)) {
                    fragDistance = vec2(max(v_SodiumExtraCylindricalDistance.x, v_SodiumExtraCylindricalDistance.y / SODIUM_EXTRA_CYLINDRICAL_VERTICAL_SCALE));
                    renderFog -= SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET;
                } else if (sodium_extra_is_shape_encoded(renderFog, SODIUM_EXTRA_PLANAR_FOG_OFFSET)) {
                    fragDistance = vec2(v_PlanarDistance);
                    renderFog -= SODIUM_EXTRA_PLANAR_FOG_OFFSET;
                } else if (sodium_extra_is_shape_encoded(renderFog, SODIUM_EXTRA_RADIAL_FOG_OFFSET)) {
                    fragDistance = vec2(fragDistance.y);
                    renderFog -= SODIUM_EXTRA_RADIAL_FOG_OFFSET;
                }

                return _linearFog(fragColor, fragDistance, fogColor, environmentFog, renderFog, fadeFactor);
            }

            """.formatted(Float.toString(FogDistanceHelper.CYLINDRICAL_VERTICAL_SCALE));

    private static final String VERTEX_PLANAR_DECL = "\nlayout(location = 4) out float v_PlanarDistance;";
    private static final String VERTEX_PLANAR_COMPUTE = "v_PlanarDistance = abs((u_ModelViewMatrix * vec4(position, 1.0)).z);\n\n    ";
    private static final String VERTEX_CYLINDRICAL_DECL = "\nlayout(location = 5) out vec2 v_SodiumExtraCylindricalDistance;";
    private static final String VERTEX_CYLINDRICAL_COMPUTE = "v_SodiumExtraCylindricalDistance = vec2(length(position.xz), abs(position.y));\n    ";
    private static final String FRAGMENT_PLANAR_DECL = "\nlayout(location = 4) in float v_PlanarDistance;";
    private static final String FRAGMENT_CYLINDRICAL_DECL = "\nlayout(location = 5) in vec2 v_SodiumExtraCylindricalDistance;";

    private static final AtomicBoolean WARNED = new AtomicBoolean(false);

    // Latched false once Sodium's terrain shader no longer contains the expected anchors.
    private static volatile boolean shapeSupported = true;

    public static boolean isShapeSupported() {
        return shapeSupported;
    }

    public static String injectRenderDistanceShape(String source) {
        if (source == null) {
            return source;
        }

        if (source.contains(PLANAR_VARYING_MARKER) && source.contains(CYLINDRICAL_VARYING_MARKER)) {
            return source;
        }

        if (source.contains(FOG_INCLUDE_ANCHOR)) {
            if (source.contains(VERTEX_DECL_ANCHOR) && source.contains(VERTEX_COMPUTE_ANCHOR)) {
                return source
                        .replace(VERTEX_DECL_ANCHOR, VERTEX_DECL_ANCHOR + VERTEX_PLANAR_DECL + VERTEX_CYLINDRICAL_DECL)
                        .replace(VERTEX_COMPUTE_ANCHOR, VERTEX_PLANAR_COMPUTE + VERTEX_CYLINDRICAL_COMPUTE + VERTEX_COMPUTE_ANCHOR);
            }

            if (source.contains(FRAGMENT_DECL_ANCHOR) && source.contains(FRAGMENT_FOG_CALL_ANCHOR)) {
                // Replace before injecting the helper; the helper contains the same fallback expression.
                return source
                        .replace(FRAGMENT_FOG_CALL_ANCHOR, FRAGMENT_FOG_CALL)
                        .replace(FRAGMENT_DECL_ANCHOR, FRAGMENT_DECL_ANCHOR + FRAGMENT_PLANAR_DECL + FRAGMENT_CYLINDRICAL_DECL + "\n" + SHAPE_HELPER);
            }
        }

        warnDrift();
        return source;
    }

    private static void warnDrift() {
        shapeSupported = false;
        if (WARNED.compareAndSet(false, true)) {
            SodiumExtraClientMod.logger().warn(
                    "Sodium's terrain fog shader no longer matches the expected layout; custom fog shapes are partly disabled. The fog shader patch needs to be re-synced with this version.");
        }
    }
}
