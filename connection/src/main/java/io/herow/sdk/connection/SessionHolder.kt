package io.herow.sdk.connection

import com.google.gson.Gson
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.userinfo.UserInfo

class SessionHolder(private val dataHolder: DataHolder) {
    val gson = Gson()

    companion object {
        private const val KEY_ACCESS_TOKEN = "common.access_token"
        private const val KEY_HEROW_ID = "common.herow_id"
        private const val KEY_DEVICE_ID = "common.device_id"
        private const val KEY_ADVERTISER_ID = "detection.ad_id"
        private const val KEY_TOKEN_TIMEOUT = "common.timeout_token"
        private const val KEY_USER_INFO = "common.user_info"
        private const val KEY_CACHE_TIMEOUT = "common.timeout_cache"
        private const val KEY_UPDATE_CACHE = "common_update_cache"
        private const val KEY_SAVED_GEOHASH = "common.saved_geohash"
    }

    fun getDeviceId(): String {
        return dataHolder.get<String>(KEY_DEVICE_ID)
    }

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

    fun getAccessToken(): String {
        return dataHolder.get<String>(KEY_ACCESS_TOKEN)
    }

    fun saveAccessToken(accessToken: String) {
        if (accessToken.isNotEmpty()) {
            dataHolder[KEY_ACCESS_TOKEN] = accessToken
        }
    }

    fun getHerowId(): String {
        return dataHolder.get<String>(KEY_HEROW_ID)
    }

    fun saveHerowId(herowId: String) {
        if (herowId.isNotEmpty()) {
            dataHolder[KEY_HEROW_ID] = herowId
        }
    }

    fun saveTimeBeforeTimeOut(time: Long) {
        dataHolder[KEY_TOKEN_TIMEOUT] = time
    }

    fun getTimeOutToken(): Long {
        return dataHolder.get<Long>(KEY_TOKEN_TIMEOUT)
    }

    fun isFirstInstance(): Boolean = !dataHolder.containsKey(KEY_USER_INFO)

    fun jsonToStringUserInfo(userInfo: String) {
        dataHolder[KEY_USER_INFO] = gson.toJson(userInfo)
    }

    fun stringToJsonUserInfo(): UserInfo? {
        return gson
            .fromJson(dataHolder.get<String>(KEY_USER_INFO), UserInfo::class.java)
    }

    fun saveModifiedCacheTime(time: Long) {
        dataHolder[KEY_CACHE_TIMEOUT] = time
    }

    private fun getModifiedCacheTime(): Long {
        return dataHolder.get<Long>(KEY_CACHE_TIMEOUT)
    }

    fun shouldCacheBeUpdated(remoteCacheTime: Long): Boolean {
        return remoteCacheTime > getModifiedCacheTime()
    }

    fun updateCache(update: Boolean) {
        dataHolder[KEY_UPDATE_CACHE] = update
    }

    fun isCacheTimeSaved(): Boolean = dataHolder.containsKey(KEY_CACHE_TIMEOUT)

    fun getUpdateCacheStatus(): Boolean {
        return dataHolder.get<Boolean>(KEY_UPDATE_CACHE)
    }

    fun saveGeohash(geoHash: String?) {
        dataHolder[KEY_SAVED_GEOHASH] = geoHash
    }

    fun getGeohash(): String {
        return dataHolder.get<String>(KEY_SAVED_GEOHASH)
    }

    fun isGeoHashSaved(): Boolean = dataHolder.containsKey(KEY_SAVED_GEOHASH)
}