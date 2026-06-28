package me.flashyreese.mods.sodiumextra.client.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.resources.Identifier;

import java.util.concurrent.atomic.AtomicBoolean;

public final class FogShaderTransformer {
    private static final Identifier SODIUM_FOG_INCLUDE = Identifier.fromNamespaceAndPath("sodium", "include/fog.glsl");
    private static final Identifier SODIUM_TERRAIN_VERTEX_SHADER = Identifier.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque.vsh");
    private static final Identifier SODIUM_TERRAIN_FRAGMENT_SHADER = Identifier.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque.fsh");
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
            return injectPlanarVarying(source);
        }

        return source;
    }

    private static String injectFogInclude(String source) {
        if (source.contains(RENDER_DISTANCE_MARKER)) {
            return source;
        }

        if (!source.contains(TOTAL_FOG_DECL) || !source.contains(CYLINDRICAL_TERM)) {
            warnDrift();
            return source;
        }

        return source
                .replace(CYLINDRICAL_TERM, RENDER_DISTANCE_CALL)
                .replace(TOTAL_FOG_DECL, SHAPE_HELPER + TOTAL_FOG_DECL);
    }

    private static String injectPlanarVarying(String source) {
        if (source.contains(PLANAR_VARYING_MARKER)) {
            return source;
        }

        if (source.contains(VERTEX_DECL_ANCHOR) && source.contains(VERTEX_COMPUTE_ANCHOR)) {
            return source
                    .replace(VERTEX_DECL_ANCHOR, VERTEX_DECL_ANCHOR + VERTEX_PLANAR_DECL)
                    .replace(VERTEX_COMPUTE_ANCHOR, VERTEX_PLANAR_COMPUTE + VERTEX_COMPUTE_ANCHOR);
        }

        if (source.contains(FRAGMENT_DECL_ANCHOR) && source.contains(FRAGMENT_FOG_CALL_ANCHOR)) {
            return source
                    .replace(FRAGMENT_DECL_ANCHOR, FRAGMENT_DECL_ANCHOR + FRAGMENT_PLANAR_DECL)
                    .replace(FRAGMENT_FOG_CALL_ANCHOR, FRAGMENT_PLANAR_ASSIGN + FRAGMENT_FOG_CALL_ANCHOR);
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
