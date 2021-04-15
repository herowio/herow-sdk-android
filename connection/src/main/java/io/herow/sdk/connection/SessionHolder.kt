package io.herow.sdk.connection

import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.userinfo.UserInfo

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
        return  dataHolder[KEY_SDK] ?: ""
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
            dataHolder.get<Boolean>(KEY_CLICK_AND_COLLECT_PROGRESS)
        }
}

