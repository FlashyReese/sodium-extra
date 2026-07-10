package me.flashyreese.mods.sodiumextra.client.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.resources.Identifier;

import java.util.concurrent.atomic.AtomicBoolean;

public final class FogShaderTransformer {
    private static final Identifier SODIUM_FOG_INCLUDE = Identifier.fromNamespaceAndPath("sodium", "include/fog.glsl");
    private static final Identifier SODIUM_TERRAIN_VERTEX_SHADER = Identifier.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque.vsh");
    private static final Identifier SODIUM_TERRAIN_FRAGMENT_SHADER = Identifier.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque.fsh");
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
    private static final String VERTEX_PLANAR_COMPUTE = "v_PlanarDistance = -(u_ModelViewMatrix * vec4(position, 1.0)).z;\n\n    ";
    private static final String VERTEX_CYLINDRICAL_DECL = "\nout vec2 v_SodiumExtraCylindricalDistance;";
    private static final String VERTEX_CYLINDRICAL_COMPUTE = "v_SodiumExtraCylindricalDistance = vec2(length(position.xz), abs(position.y));\n    ";
    private static final String FRAGMENT_PLANAR_DECL = "\nin float v_PlanarDistance;";
    private static final String FRAGMENT_PLANAR_ASSIGN = "sodiumExtra_planarDistance = v_PlanarDistance;\n    ";
    private static final String FRAGMENT_CYLINDRICAL_DECL = "\nin vec2 v_SodiumExtraCylindricalDistance;";
    private static final String FRAGMENT_CYLINDRICAL_ASSIGN = "sodiumExtra_cylindricalDistance = v_SodiumExtraCylindricalDistance;\n    ";

    private static final AtomicBoolean WARNED = new AtomicBoolean(false);

    private static volatile boolean shapeSupported = true;

    public static boolean isShapeSupported() {
        return shapeSupported;
    }

    public static String injectSodiumShaderSource(String source, Identifier location) {
        if (source == null || location == null) {
            return source;
        }

        if (location.equals(SODIUM_FOG_INCLUDE)) {
            return injectFogInclude(source);
        }

        if (location.equals(SODIUM_TERRAIN_VERTEX_SHADER) || location.equals(SODIUM_TERRAIN_FRAGMENT_SHADER)) {
            return injectTerrainVaryings(source);
        }

        return source;
    }

    private static String injectFogInclude(String source) {
        if (source.contains(TOTAL_FOG_MARKER)) {
            return source;
        }

        if (!source.contains(TOTAL_FOG_DECL) || !source.contains(TOTAL_FOG_RETURN)) {
            warnDrift();
            return source;
        }

        return source
                .replace(TOTAL_FOG_RETURN, TOTAL_FOG_CALL)
                .replace(TOTAL_FOG_DECL, SHAPE_HELPER + TOTAL_FOG_DECL);
    }

    private static String injectTerrainVaryings(String source) {
        boolean needsPlanarVarying = !source.contains(PLANAR_VARYING_MARKER);
        boolean needsCylindricalVarying = !source.contains(CYLINDRICAL_VARYING_MARKER);
        if (!needsPlanarVarying && !needsCylindricalVarying) {
            return source;
        }

        if (source.contains(VERTEX_DECL_ANCHOR) && source.contains(VERTEX_COMPUTE_ANCHOR)) {
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

            return source
                    .replace(VERTEX_DECL_ANCHOR, VERTEX_DECL_ANCHOR + declarations)
                    .replace(VERTEX_COMPUTE_ANCHOR, computations + VERTEX_COMPUTE_ANCHOR);
        }

        if (source.contains(FRAGMENT_DECL_ANCHOR) && source.contains(FRAGMENT_FOG_CALL_ANCHOR)) {
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

            return source
                    .replace(FRAGMENT_DECL_ANCHOR, FRAGMENT_DECL_ANCHOR + declarations)
                    .replace(FRAGMENT_FOG_CALL_ANCHOR, assignments + FRAGMENT_FOG_CALL_ANCHOR);
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
