package io.herow.sdk.common.json

import android.location.Location
import com.google.gson.*
import java.lang.reflect.Type

class LocationAdapter: JsonSerializer<Location>, JsonDeserializer<Location> {
    override fun serialize(location: Location?,
                           typeOfSrc: Type?,
                           context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        location?.run {
            jsonObject.addProperty("lat", this.latitude)
            jsonObject.addProperty("lng", this.longitude)
            jsonObject.addProperty("alt", this.altitude)
            jsonObject.addProperty("horizontalAccuracy", this.accuracy)
            jsonObject.addProperty("speed", this.speed)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                jsonObject.addProperty("verticalAccuracy", this.verticalAccuracyMeters)
                jsonObject.addProperty("speedAccuracy", this.speedAccuracyMetersPerSecond)
                jsonObject.addProperty("bearingAccuracy", this.bearingAccuracyDegrees)
            }
            jsonObject.addProperty("bearing", this.bearing)
            jsonObject.addProperty("timestamp", this.time)
            jsonObject.addProperty("provider", this.provider)
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Location {
        val jsonObject: JsonObject? = json as? JsonObject
        val location = Location(jsonObject?.get("provider")?.asString ?: "unknown")
        jsonObject?.run {
            location.latitude = this["lng"].asDouble
            location.longitude = this["lng"].asDouble
            location.altitude = this["alt"].asDouble
            location.accuracy = this["horizontalAccuracy"].asFloat
            location.speed = this["speed"].asFloat
            location.bearing = this["mBearing"].asFloat
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                location.verticalAccuracyMeters = this["verticalAccuracy"].asFloat
                location.speedAccuracyMetersPerSecond = this["speedAccuracy"].asFloat
                location.bearingAccuracyDegrees = this["bearingAccuracy"].asFloat
            }
            location.bearing = this["bearing"].asFloat
            location.time = this["timestamp"].asLong
        }
        return location
    }
}