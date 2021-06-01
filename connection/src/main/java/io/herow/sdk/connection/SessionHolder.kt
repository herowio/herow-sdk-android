package io.herow.sdk.connection

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.model.mapper.HerowCappingMapper
import io.herow.sdk.connection.userinfo.UserInfo
import java.lang.reflect.Type

class SessionHolder(private val dataHolder: DataHolder) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "common.access_token"
        private const val KEY_HEROW_ID = "common.herow_id"
        private const val KEY_DEVICE_ID = "common.device_id"
        private const val KEY_CUSTOM_ID = "common.custom_id"
        private const val KEY_ADVERTISER_ID = "detection.ad_id"
        private const val KEY_TOKEN_TIMEOUT = "common.timeout_token"
        private const val KEY_USER_INFO = "common.user_info"
        private const val KEY_CACHE_TIMEOUT = "common.timeout_cache"
        private const val KEY_UPDATE_CACHE = "common_update_cache"
        private const val KEY_SAVED_GEOHASH = "common.saved_geohash"
        private const val KEY_REPEAT_INTERVAL = "common.repeat_interval"
        private const val KEY_OPTIN = "common.optin"
        private const val KEY_SDK = "common.sdk"
        private const val KEY_CLICK_AND_COLLECT_PROGRESS = "detection.click_and_collect_progress"
        private const val KEY_HEROW_CAPPING = "detection.herow_capping"
        private const val KEY_LAUNCH_CONFIG = "detection.config_request"
        private const val KEY_SAVED_PREVIOUS_ZONES = "detection.previous_zones"
        private const val KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION = "detection.previous_zones_for_notification"
    }

    fun getDeviceId(): String = dataHolder[KEY_DEVICE_ID]

    fun saveDeviceId(deviceId: String) {
        if (deviceId.isNotEmpty()) {
            dataHolder[KEY_DEVICE_ID] = deviceId
        }
    }

    fun getAdvertiserId(): String? {
        val advertiserId = dataHolder.get<String>(KEY_ADVERTISER_ID)
        if (advertiserId.isNotEmpty()) {
            return advertiserId
        }
        return null
    }

    fun saveAdvertiserId(advertiserId: String) {
        if (advertiserId.isNotEmpty()) {
            dataHolder[KEY_ADVERTISER_ID] = advertiserId
        }
    }

    fun getAccessToken(): String = dataHolder[KEY_ACCESS_TOKEN]

    fun saveAccessToken(accessToken: String) {
        if (accessToken.isNotEmpty()) {
            dataHolder[KEY_ACCESS_TOKEN] = accessToken
        }
    }

    fun getHerowId(): String = dataHolder[KEY_HEROW_ID]

    fun saveHerowId(herowId: String) {
        if (herowId.isNotEmpty()) {
            dataHolder[KEY_HEROW_ID] = herowId
        }
    }

    fun getCustomID(): String {
        val customID = dataHolder.get<String>(KEY_CUSTOM_ID)
        if (customID.isNotEmpty()) {
            return customID
        }
        return ""
    }

    fun saveCustomID(customID: String) {
        if (customID.isNotEmpty()) {
            dataHolder[KEY_CUSTOM_ID] = customID
        }
    }

    fun saveTimeOutTime(time: Long) {
        dataHolder[KEY_TOKEN_TIMEOUT] = time
    }

    fun getTimeOutTime(): Long = dataHolder[KEY_TOKEN_TIMEOUT]

    fun hasNoUserInfoSaved(): Boolean = !dataHolder.containsKey(KEY_USER_INFO)

    fun saveStringUserInfo(userInfo: String) {
        dataHolder[KEY_USER_INFO] = userInfo
    }

    fun loadSaveStringToUserInfo(): UserInfo {
        val savedUserInfo = dataHolder.get<String>(KEY_USER_INFO)
        return GsonProvider.fromJson(savedUserInfo, UserInfo::class.java)
    }

    fun saveModifiedCacheTime(date: String) {
        dataHolder[KEY_CACHE_TIMEOUT] = date
    }

    fun getLastSavedModifiedDateTimeCache(): String = dataHolder[KEY_CACHE_TIMEOUT]

    fun updateCache(update: Boolean) {
        dataHolder[KEY_UPDATE_CACHE] = update
    }

    fun getUpdateCacheStatus(): Boolean {
        return dataHolder[KEY_UPDATE_CACHE]
    }

    fun hasNoCacheTimeSaved(): Boolean = !dataHolder.containsKey(KEY_CACHE_TIMEOUT)

    fun saveGeohash(geoHash: String?) {
        dataHolder[KEY_SAVED_GEOHASH] = geoHash
    }

    fun getGeohash(): String = dataHolder[KEY_SAVED_GEOHASH]

    fun hasNoGeoHashSaved(): Boolean = !dataHolder.containsKey(KEY_SAVED_GEOHASH)

    fun hasNoRepeatIntervalSaved(): Boolean = !dataHolder.containsKey(KEY_REPEAT_INTERVAL)

    fun saveRepeatInterval(repeatInterval: Long) {
        dataHolder[KEY_REPEAT_INTERVAL] = repeatInterval
    }

    fun getRepeatInterval(): Long = dataHolder[KEY_REPEAT_INTERVAL]

    fun saveOptinValue(optinAccepted: Boolean?) {
        dataHolder[KEY_OPTIN] = optinAccepted ?: false
    }

    fun saveSDKID(sdkID: String) {
        dataHolder[KEY_SDK] = sdkID
    }

    fun getSDKID(): String {
        return dataHolder[KEY_SDK]
    }

    fun getOptinValue(): Boolean =
        if (dataHolder.containsKey(KEY_OPTIN)) {
            dataHolder[KEY_OPTIN]
        } else {
            false
        }

    fun saveClickAndCollectProgress(progress: Boolean) {
        dataHolder[KEY_CLICK_AND_COLLECT_PROGRESS] = progress
    }

    fun getClickAndCollectProgress(): Boolean =
        if (!dataHolder.containsKey(KEY_CLICK_AND_COLLECT_PROGRESS)) {
            false
        } else {
            dataHolder[KEY_CLICK_AND_COLLECT_PROGRESS]
        }

    fun hasHerowCappingSaved(): Boolean = dataHolder.containsKey(KEY_HEROW_CAPPING)

    fun saveHerowCapping(herowCapping: String) {
        dataHolder[KEY_HEROW_CAPPING] = herowCapping
    }

    fun getHerowCapping(): HerowCappingMapper =
        GsonProvider.fromJson(dataHolder[KEY_HEROW_CAPPING], HerowCappingMapper::class.java)

    fun firstTimeLaunchingConfig(): Boolean = !dataHolder.containsKey(KEY_LAUNCH_CONFIG)

    fun saveConfigLaunch(time: Long) {
        dataHolder[KEY_LAUNCH_CONFIG] = time
    }

    fun getLastConfigLaunch(): Long = dataHolder[KEY_LAUNCH_CONFIG]
    
    fun hasPreviousZones(): Boolean = dataHolder.containsKey(KEY_SAVED_PREVIOUS_ZONES)
    
    fun hasPreviousZonesForNotification(): Boolean = dataHolder.containsKey(
        KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION)

    fun savePreviousZones(zones: List<Zone>) {
        dataHolder[KEY_SAVED_PREVIOUS_ZONES] = Gson().toJson(zones)
    }

    fun savePreviousZonesForNotification(zonesForNotification: List<Zone>) {
        dataHolder[KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION] = Gson().toJson(zonesForNotification)
    }

    fun getSavedPreviousZones(): ArrayList<Zone> {
        val savedPreviousZones = dataHolder.get<String>(KEY_SAVED_PREVIOUS_ZONES)

        val listType: Type = object :
            TypeToken<ArrayList<Zone>?>() {}.type
        return Gson().fromJson(savedPreviousZones, listType)
    }

    fun getSavedPreviousZonesForNotification(): ArrayList<Zone> {
        val savedPreviousZonesForNotification = dataHolder.get<String>(KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION)

        val listType: Type = object :
            TypeToken<ArrayList<Zone>?>() {}.type
        return Gson().fromJson(savedPreviousZonesForNotification, listType)
    }

    fun clearPreviousZones() = dataHolder.removeKey(KEY_SAVED_PREVIOUS_ZONES)

    fun clearPreviousZonesForNotification() = dataHolder.removeKey(
        KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION)
}