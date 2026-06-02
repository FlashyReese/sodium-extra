package me.flashyreese.mods.sodiumextra.common.util;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

public class IdentifierSerializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
    public IdentifierSerializer() {
    }

    public ResourceLocation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return ResourceLocation.parse(GsonHelper.convertToString(jsonElement, "location"));
    }

    public JsonElement serialize(ResourceLocation resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(resourceLocation.toString());
    }
}