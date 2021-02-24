package io.herow.sdk.connection.cache.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.herow.sdk.connection.cache.model.*
import java.lang.reflect.Type
import java.util.*

class Converters {

    @TypeConverter
    fun stringToListIntervals(data: String?): List<Interval?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object :
            TypeToken<List<Interval?>?>() {}.type
        return Gson().fromJson<List<Interval?>>(data, listType)
    }

    @TypeConverter
    fun listServerToString(someObjects: List<Interval?>?): String? {
        return Gson().toJson(someObjects)
    }

    @TypeConverter
    fun stringToCampaign(string: String) = Gson().fromJson(string, Campaign::class.java)

    @TypeConverter
    fun campaignToString(campaign: Campaign) = Gson().toJson(campaign)

    @TypeConverter
    fun stringToAccess(string: String) = Gson().fromJson(string, Access::class.java)

    @TypeConverter
    fun accessToString(access: Access) = Gson().toJson(access)

    @TypeConverter
    fun stringToCapping(string: String?) = Gson().fromJson(string, Capping::class.java) ?: null

    @TypeConverter
    fun cappingToString(capping: Capping?) = Gson().toJson(capping) ?: ""

    @TypeConverter
    fun stringToNotification(string: String) = Gson().fromJson(string, Notification::class.java)

    @TypeConverter
    fun notificationToString(notification: Notification?) = Gson().toJson(notification) ?: ""

    @TypeConverter
    fun stringToPoi(string: String) = Gson().fromJson(string, Poi::class.java)

    @TypeConverter
    fun poiToString(poi: Poi) = Gson().toJson(poi)

    @TypeConverter
    fun stringToTrigger(string: String) = Gson().fromJson(string, Trigger::class.java)

    @TypeConverter
    fun triggerToString(trigger: Trigger?) = Gson().toJson(trigger) ?: ""

    @TypeConverter
    fun stringToZone(string: String) = Gson().fromJson(string, Zone::class.java)

    @TypeConverter
    fun zoneToString(zone: Zone) = Gson().toJson(zone)

    @TypeConverter
    fun stringToTags(string: String?) = string!!.split(",").map { it }

    @TypeConverter
    fun listTagsToString(tags: List<String>?) = Gson().toJson(tags)
}