package me.flashyreese.mods.sodiumextra.client.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicBoolean;

public final class FogShaderTransformer {
    private static final ResourceLocation SODIUM_FOG_INCLUDE = ResourceLocation.fromNamespaceAndPath("sodium", "include/fog.glsl");
    private static final ResourceLocation SODIUM_TERRAIN_VERTEX_SHADER = ResourceLocation.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque.vsh");
    private static final ResourceLocation SODIUM_TERRAIN_FRAGMENT_SHADER = ResourceLocation.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque.fsh");
    private static final String SHAPE_HELPER_MARKER = "sodiumExtra_fogDistance";
    private static final String PLANAR_VARYING_MARKER = "v_PlanarDistance";
    private static final String CYLINDRICAL_VARYING_MARKER = "v_SodiumExtraCylindricalDistance";
    private static final String LINEAR_FOG_ANCHOR = "vec4 _linearFog(vec4 fragColor, float fragDistance, vec4 fogColor, float fogStart, float fogEnd) {";
    private static final String LINEAR_FOG_BODY_ANCHOR = LINEAR_FOG_ANCHOR + "\n#ifdef USE_FOG\n";
    private static final String VERTEX_DECL_ANCHOR = "out float v_FragDistance;";
    private static final String VERTEX_COMPUTE_ANCHOR = "gl_Position = u_ProjectionMatrix * u_ModelViewMatrix * vec4(position, 1.0);";
    private static final String FRAGMENT_DECL_ANCHOR = "in float v_FragDistance;";
    private static final String FRAGMENT_FOG_CALL_ANCHOR = "fragColor = _linearFog(";

    private static final String SHAPE_HELPER = """
            const float SODIUM_EXTRA_PLANAR_FOG_OFFSET = 2097152.0;
            const float SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET = 3145728.0;
            const float SODIUM_EXTRA_CYLINDRICAL_VERTICAL_SCALE = %s;

            float sodiumExtra_planarDistance = 0.0;
            vec2 sodiumExtra_cylindricalDistance = vec2(0.0);

            float sodiumExtra_cylindricalFogDistance(float horizontalDistance, float verticalDistance) {
                return max(horizontalDistance, verticalDistance / SODIUM_EXTRA_CYLINDRICAL_VERTICAL_SCALE);
            }

            float sodiumExtra_fogDistance(float fragDistance, float fogStart, float fogEnd) {
                if (fogStart >= SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET && fogEnd >= SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET) {
                    return sodiumExtra_cylindricalFogDistance(sodiumExtra_cylindricalDistance.x, sodiumExtra_cylindricalDistance.y);
                }

                if (fogStart >= SODIUM_EXTRA_PLANAR_FOG_OFFSET && fogEnd >= SODIUM_EXTRA_PLANAR_FOG_OFFSET) {
                    return sodiumExtra_planarDistance;
                }

                return fragDistance;
            }

            float sodiumExtra_fogStart(float fogStart) {
                if (fogStart >= SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET) {
                    return fogStart - SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET;
                }

                return fogStart >= SODIUM_EXTRA_PLANAR_FOG_OFFSET ? fogStart - SODIUM_EXTRA_PLANAR_FOG_OFFSET : fogStart;
            }

            float sodiumExtra_fogEnd(float fogEnd) {
                if (fogEnd >= SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET) {
                    return fogEnd - SODIUM_EXTRA_CYLINDRICAL_FOG_OFFSET;
                }

                return fogEnd >= SODIUM_EXTRA_PLANAR_FOG_OFFSET ? fogEnd - SODIUM_EXTRA_PLANAR_FOG_OFFSET : fogEnd;
            }

            """.formatted(Float.toString(FogDistanceHelper.CYLINDRICAL_VERTICAL_SCALE));

    private static final String LINEAR_FOG_SETUP = """
                fragDistance = sodiumExtra_fogDistance(fragDistance, fogStart, fogEnd);
                fogStart = sodiumExtra_fogStart(fogStart);
                fogEnd = sodiumExtra_fogEnd(fogEnd);
            """;

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

    private FogShaderTransformer() {
    }

    public static boolean isShapeSupported() {
        return shapeSupported;
    }

    public static String injectSodiumShaderSource(String source, ResourceLocation location) {
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
        if (source.contains(SHAPE_HELPER_MARKER)) {
            return source;
        }

        if (!source.contains(LINEAR_FOG_BODY_ANCHOR)) {
            warnDrift();
            return source;
        }

        return source.replace(LINEAR_FOG_BODY_ANCHOR, SHAPE_HELPER + LINEAR_FOG_ANCHOR + "\n#ifdef USE_FOG\n" + LINEAR_FOG_SETUP);
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
                    "Sodium's terrain fog shader no longer matches the expected layout; custom fog shapes are disabled. The fog shader patch needs to be re-synced with this version.");
        }
    }
}
