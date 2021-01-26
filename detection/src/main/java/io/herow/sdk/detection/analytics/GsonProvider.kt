package io.herow.sdk.detection.analytics

import android.location.Location
import com.google.gson.GsonBuilder
import io.herow.sdk.detection.analytics.adapter.LocationAdapter
import java.lang.reflect.Type

/**
 * We use only one instance of the gson class.
 */
object GsonProvider {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Location::class.java, LocationAdapter())
        .create()

    fun toJson(src: Any, typeOfSrc: Type): String {
        return gson.toJson(src, typeOfSrc)
    }

    fun <T> fromJson(json: String, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }
}