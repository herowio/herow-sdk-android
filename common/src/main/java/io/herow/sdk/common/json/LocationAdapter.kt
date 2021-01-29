package io.herow.sdk.common.json

import android.location.Location
import com.google.gson.*
import java.lang.reflect.Type

class LocationAdapter: JsonSerializer<Location>, JsonDeserializer<Location> {
    override fun serialize(location: Location?,
                           typeOfSrc: Type?,
                           context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        location?.let {
            jsonObject.addProperty("lat", location.latitude)
            jsonObject.addProperty("lng", location.longitude)
            jsonObject.addProperty("alt", location.altitude)
            jsonObject.addProperty("horizontalAccuracy", location.accuracy)
            jsonObject.addProperty("speed", location.speed)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                jsonObject.addProperty("verticalAccuracy", location.verticalAccuracyMeters)
                jsonObject.addProperty("speedAccuracy", location.speedAccuracyMetersPerSecond)
                jsonObject.addProperty("bearingAccuracy", location.bearingAccuracyDegrees)
            }
            jsonObject.addProperty("bearing", location.bearing)
            jsonObject.addProperty("timestamp", location.time)
            jsonObject.addProperty("provider", location.provider)
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Location {
        val jsonObject: JsonObject? = json as? JsonObject
        val location = Location(jsonObject?.get("provider")?.asString ?: "unknown")
        jsonObject?.let {
            location.latitude = it["lng"].asDouble
            location.longitude = it["lng"].asDouble
            location.altitude = it["alt"].asDouble
            location.accuracy = it["horizontalAccuracy"].asFloat
            location.speed = it["speed"].asFloat
            location.bearing = it["mBearing"].asFloat
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                location.verticalAccuracyMeters = it["verticalAccuracy"].asFloat
                location.speedAccuracyMetersPerSecond = it["speedAccuracy"].asFloat
                location.bearingAccuracyDegrees = it["bearingAccuracy"].asFloat
            }
            location.bearing = it["bearing"].asFloat
            location.time = it["timestamp"].asLong
        }
        return location
    }
}