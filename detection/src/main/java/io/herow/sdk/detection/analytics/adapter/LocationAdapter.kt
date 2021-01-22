package io.herow.sdk.detection.analytics.adapter

import android.location.Location
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.json.JSONException
import org.json.JSONObject

class LocationAdapter {
    @FromJson
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

    @ToJson
    fun toJson(location: Location): String {
        val jsonObject = JSONObject()
        jsonObject.put("lat", location.latitude)
        jsonObject.put("lng", location.longitude)
        jsonObject.put("alt", location.altitude)
        jsonObject.put("horizontalAccuracy", location.accuracy)
        jsonObject.put("speed", location.speed)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            jsonObject.put("verticalAccuracy", location.verticalAccuracyMeters)
            jsonObject.put("speedAccuracy", location.speedAccuracyMetersPerSecond)
            jsonObject.put("bearingAccuracy", location.bearingAccuracyDegrees)
        }
        jsonObject.put("bearing", location.bearing)
        jsonObject.put("timestamp", location.time)
        jsonObject.put("provider", location.provider)
        return jsonObject.toString()
    }
}