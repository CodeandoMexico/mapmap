package org.codeandomexico.mapmap.server.util;

import com.google.gson.*;
import org.codeandomexico.mapmap.server.model.TripPattern;
import org.codeandomexico.mapmap.server.model.TripPatternStop;

import java.lang.reflect.Type;

public class TripPatternSerializer implements JsonSerializer<TripPattern> {

    @Override
    public JsonElement serialize(TripPattern tripPattern, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", tripPattern.id);
        jsonObject.addProperty("name", tripPattern.name);
        jsonObject.addProperty("headsign", tripPattern.headsign);
        // jsonObject.addProperty("color", tripPattern.route.agency.color);
        // jsonObject.addProperty("gtfsAgencyId", tripPattern.route.agency.gtfsAgencyId);
        jsonObject.addProperty("routeShortName", tripPattern.route.routeShortName);
        jsonObject.addProperty("routeLongName", tripPattern.route.routeLongName);
        jsonObject.addProperty("routeDescription", tripPattern.route.routeDesc);
        jsonObject.addProperty("routeNotes", tripPattern.route.routeNotes);
        jsonObject.addProperty("headsign", tripPattern.headsign);

        JsonObject jsonBoundsObject = new JsonObject();
        jsonBoundsObject.addProperty("lat1", tripPattern.tripShape.shape.getEnvelopeInternal().getMaxY());
        jsonBoundsObject.addProperty("lon1", tripPattern.tripShape.shape.getEnvelopeInternal().getMaxX());
        jsonBoundsObject.addProperty("lat2", tripPattern.tripShape.shape.getEnvelopeInternal().getMinY());
        jsonBoundsObject.addProperty("lon2", tripPattern.tripShape.shape.getEnvelopeInternal().getMinX());

        jsonObject.add("bounds", jsonBoundsObject);

        JsonArray jsonArray = new JsonArray();

        // List<TripPatternStop> tripPatternStops = TripPatternStop.find("pattern = ? ORDER BY id", tripPattern).fetch();

        for (TripPatternStop tripPatternStop : tripPattern.patternStops) {
            JsonObject jsonTpsObject = new JsonObject();
            jsonTpsObject.addProperty("lat", tripPatternStop.stop.location.getY());
            jsonTpsObject.addProperty("lon", tripPatternStop.stop.location.getX());
            jsonTpsObject.addProperty("board", tripPatternStop.board);
            jsonTpsObject.addProperty("alight", tripPatternStop.alight);
            jsonTpsObject.addProperty("travelTime", tripPatternStop.defaultTravelTime);
            jsonArray.add(jsonTpsObject);
        }
        jsonObject.add("stops", jsonArray);

        EncodedPolylineBean polyline = PolylineEncoder.createEncodings(tripPattern.tripShape.shape);
        jsonObject.addProperty("shape", polyline.getPoints());

        return jsonObject;
    }
}
