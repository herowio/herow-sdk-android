package io.herow.sdk.common.json

import android.location.Location
import androidx.annotation.Keep
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

/**
 * We use only one instance of the gson class.
 */
@Keep
object GsonProvider {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Location::class.java, LocationAdapter())
        .create()

    fun toJson(src: Any): String {
        return gson.toJson(src)
    }

    fun toJson(src: Any, typeOfSrc: Type): String {
        return gson.toJson(src, typeOfSrc)
    }

    fun <T> fromJson(json: String, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }
}