package io.herow.sdk.connection

import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.userinfo.UserInfo

class SessionHolder(private val dataHolder: DataHolder) {
    companion object {
        private const val KEY_ACCESS_TOKEN = "common.access_token"
        private const val KEY_HEROW_ID = "common.herow_id"
        private const val KEY_DEVICE_ID = "common.device_id"
        private const val KEY_ADVERTISER_ID = "detection.ad_id"
        private const val KEY_TOKEN_TIMEOUT = "common.timeout_token"
        private const val KEY_USER_INFO = "common.user_info"
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

    fun hasNoUserInfoSaved(): Boolean = !dataHolder.containsKey(KEY_USER_INFO)

    fun saveStringUserInfo(userInfo: String) {
        dataHolder[KEY_USER_INFO] = userInfo
    }

    fun loadSaveStringToUserInfo(): UserInfo {
        val savedUserInfo = dataHolder.get<String>(KEY_USER_INFO)
        return GsonProvider.fromJson(savedUserInfo, UserInfo::class.java)
    }
}