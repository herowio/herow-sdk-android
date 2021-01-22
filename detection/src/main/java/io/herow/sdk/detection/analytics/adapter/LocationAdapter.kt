package io.herow.sdk.detection.analytics.adapter

import android.location.Location
import com.google.gson.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type

class LocationAdapter: JsonSerializer<Location>, JsonDeserializer<Location> {
    fun fromJson(stringLocation: String): Location {
        try {
            val jsonObject = JSONObject(stringLocation)
            val location = Location(jsonObject.getString("provider"))
            location.latitude = jsonObject.getDouble("lat")
            location.longitude = jsonObject.getDouble("lng")
            location.altitude = jsonObject.getDouble("alt")
            location.accuracy = jsonObject.getDouble("horizontalAccuracy").toFloat()
            location.speed = jsonObject.getDouble("speed").toFloat()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                location.verticalAccuracyMeters = jsonObject.getDouble("verticalAccuracy").toFloat()
                location.speedAccuracyMetersPerSecond = jsonObject.getDouble("speedAccuracy").toFloat()
                location.bearingAccuracyDegrees = jsonObject.getDouble("bearingAccuracy").toFloat()
            }
            location.bearing = jsonObject.getDouble("bearing").toFloat()
            location.time = jsonObject.getLong("timestamp")
            location.provider = jsonObject.getString("provider")
            return location
        } catch (jsonException: JSONException) {
            println(jsonException)
        }
        return Location("")
    }

    override fun serialize(location: Location?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
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

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Location {
        return Location("")
    }
}