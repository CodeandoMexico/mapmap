package org.codeandomexico.mapmap.server.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.codeandomexico.mapmap.server.model.TripPattern;

import java.lang.reflect.Type;

public class TripPatternListSerializer implements JsonSerializer<TripPattern> {

    @Override
    public JsonElement serialize(TripPattern tripPattern, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", tripPattern.id);
        jsonObject.addProperty("headsign", tripPattern.headsign);
        return jsonObject;
    }
}
