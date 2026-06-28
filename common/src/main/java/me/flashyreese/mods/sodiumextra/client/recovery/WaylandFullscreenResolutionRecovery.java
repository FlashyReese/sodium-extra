package me.flashyreese.mods.sodiumextra.client.recovery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class WaylandFullscreenResolutionRecovery {
    private static final Logger LOGGER = LoggerFactory.getLogger("Sodium Extra Recovery");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String FULLSCREEN = "fullscreen:";
    private static final String FULLSCREEN_RESOLUTION = "fullscreenResolution:";

    public static void recoverIfNeeded(Path gameDirectory, Path configDirectory) {
        if (!isWaylandSession()) {
            return;
        }

        Path configFile = configDirectory.resolve(SodiumExtraConfigKeys.FILE_NAME);
        JsonObject root = readJsonObject(configFile);
        if (root == null) {
            return;
        }

        JsonObject extraSettings = getObject(root, SodiumExtraConfigKeys.EXTRA_SETTINGS);
        if (extraSettings == null || !readBoolean(extraSettings, SodiumExtraConfigKeys.WAYLAND_FULLSCREEN_RESOLUTION_RECOVERY_PENDING)) {
            return;
        }

        extraSettings.addProperty(SodiumExtraConfigKeys.WAYLAND_FULLSCREEN_RESOLUTION, false);
        extraSettings.addProperty(SodiumExtraConfigKeys.WAYLAND_FULLSCREEN_RESOLUTION_RECOVERY_PENDING, false);
        writeJson(configFile, root);

        boolean changedOptions = recoverMinecraftOptions(gameDirectory.resolve("options.txt"));
        LOGGER.warn("Recovered from a pending experimental Wayland/XWayland fullscreen resolution change{}",
                changedOptions ? " and reset Minecraft fullscreen state" : "");
    }

    private static boolean recoverMinecraftOptions(Path optionsFile) {
        if (!Files.exists(optionsFile)) {
            return false;
        }

        try {
            List<String> input = Files.readAllLines(optionsFile, StandardCharsets.UTF_8);
            List<String> output = new ArrayList<>(input.size());
            boolean changed = false;
            boolean sawFullscreen = false;

            for (String line : input) {
                if (line.startsWith(FULLSCREEN_RESOLUTION)) {
                    changed = true;
                    continue;
                }

                if (line.startsWith(FULLSCREEN)) {
                    sawFullscreen = true;
                    if (!line.equals("fullscreen:false")) {
                        changed = true;
                    }
                    output.add("fullscreen:false");
                    continue;
                }

                output.add(line);
            }

            if (!sawFullscreen) {
                output.add("fullscreen:false");
                changed = true;
            }

            if (changed) {
                Files.write(optionsFile, output, StandardCharsets.UTF_8);
            }

            return changed;
        } catch (IOException e) {
            LOGGER.error("Failed to recover Minecraft fullscreen options", e);
            return false;
        }
    }

    private static JsonObject readJsonObject(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            return element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (Exception e) {
            LOGGER.error("Failed to read Sodium Extra options for fullscreen recovery", e);
            return null;
        }
    }

    private static void writeJson(Path path, JsonObject object) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(object) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to write Sodium Extra fullscreen recovery state", e);
        }
    }

    private static JsonObject getObject(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static boolean readBoolean(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsBoolean();
    }

    private static boolean isWaylandSession() {
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        return System.getenv("WAYLAND_DISPLAY") != null || "wayland".equalsIgnoreCase(sessionType);
    }
}
