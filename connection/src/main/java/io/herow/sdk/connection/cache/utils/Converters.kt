package io.herow.sdk.connection.cache.utils

import android.location.Location
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.herow.sdk.connection.cache.model.*
import java.lang.reflect.Type
import java.util.*

class Converters {

    @TypeConverter
    fun stringToListCampaigns(data: String?): List<Campaign>? {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType: Type = object : TypeToken<List<Campaign>?>() {}.type
        return Gson().fromJson<List<Campaign>?>(data, listType)
    }

    @TypeConverter
    fun listCampaignToString(someobjects: List<Campaign>?): String? = Gson().toJson(someobjects)

    @TypeConverter
    fun stringToListHerowNotifications(data: String?): List<HerowNotification>? {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType: Type = object : TypeToken<List<HerowNotification>?>() {}.type
        return Gson().fromJson<List<HerowNotification>?>(data, listType)
    }

    @TypeConverter
    fun listHerowNotificationToString(someObjects: List<HerowNotification>?): String? = Gson().toJson(someObjects)

    @TypeConverter
    fun stringToListString(data: String?): List<String>? {
        if (data.isNullOrEmpty()) {
            return Collections.emptyList()
        }
        val listType: Type = object :
            TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>?>(data, listType)
    }

    @TypeConverter
    fun listStringToString(someObjects: List<String>?): String? = Gson().toJson(someObjects)

    @TypeConverter
    fun arrayListStringToString(someObjects: ArrayList<String>?): String? = Gson().toJson(someObjects)

    @TypeConverter
    fun stringToArrayListString(data: String?): ArrayList<String>? {
        if (data == null) {
            return arrayListOf()
        }
        val listType: Type = object :
            TypeToken<ArrayList<String>?>() {}.type
        return Gson().fromJson<ArrayList<String>?>(data, listType)
    }

    @TypeConverter
    fun stringToCampaign(string: String): Campaign = Gson().fromJson(string, Campaign::class.java)

    @TypeConverter
    fun campaignToString(campaign: Campaign): String = Gson().toJson(campaign)

    @TypeConverter
    fun stringToAccess(string: String): Access = Gson().fromJson(string, Access::class.java)

    @TypeConverter
    fun accessToString(access: Access): String = Gson().toJson(access)

    @TypeConverter
    fun stringToCapping(string: String?): Capping? =
        Gson().fromJson(string, Capping::class.java) ?: null

    @TypeConverter
    fun cappingToString(capping: Capping?): String = Gson().toJson(capping) ?: ""

    @TypeConverter
    fun stringToNotification(string: String): HerowNotification =
        Gson().fromJson(string, HerowNotification::class.java)

    @TypeConverter
    fun notificationToString(notification: HerowNotification?): String =
        Gson().toJson(notification) ?: ""

    @TypeConverter
    fun stringToPoi(string: String): Poi = Gson().fromJson(string, Poi::class.java)

    @TypeConverter
    fun poiToString(poi: Poi): String = Gson().toJson(poi)

    @TypeConverter
    fun stringToZone(string: String): Zone = Gson().fromJson(string, Zone::class.java)

    @TypeConverter
    fun zoneToString(zone: Zone): String = Gson().toJson(zone)

    @TypeConverter
    fun stringToLocation(string: String?): Location? =
        Gson().fromJson(string, Location::class.java) ?: null

    @TypeConverter
    fun locationToString(location: Location?): String = Gson().toJson(location) ?: ""
}