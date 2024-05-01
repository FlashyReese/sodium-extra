package me.flashyreese.mods.sodiumextra.compat;

import net.minecraft.client.render.VertexFormat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class IrisCompat {
    private static boolean irisPresent;
    private static MethodHandle handleRenderingShadowPass;
    private static Object apiInstance;
    private static VertexFormat terrainFormat;

    static {
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            apiInstance = api.cast(api.getDeclaredMethod("getInstance").invoke(null));
            handleRenderingShadowPass = MethodHandles.lookup().findVirtual(api, "isRenderingShadowPass", MethodType.methodType(boolean.class));

            Class<?> irisVertexFormatsClass = Class.forName("net.irisshaders.iris.vertices.IrisVertexFormats");
            Field terrainField = irisVertexFormatsClass.getDeclaredField("TERRAIN");
            terrainFormat = (VertexFormat) terrainField.get(null);

            irisPresent = true;
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            irisPresent = false;
        }
    }

    public static boolean isRenderingShadowPass() {
        if (irisPresent) {
            try {
                return (boolean) handleRenderingShadowPass.invoke(apiInstance);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return false;
    }

    public static VertexFormat getTerrainFormat() {
        return terrainFormat;
    }

    public static boolean isIrisPresent() {
        return irisPresent;
    }
}