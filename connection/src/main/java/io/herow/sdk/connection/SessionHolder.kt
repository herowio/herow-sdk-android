package io.herow.sdk.connection

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.data.TagPrediction
import io.herow.sdk.common.data.ZonePrediction
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.HerowCapping
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.prediction.Prediction
import io.herow.sdk.connection.userinfo.UserInfo
import java.lang.reflect.Type

class SessionHolder(private val dataHolder: DataHolder) {

    companion object {
        private const val KEY_ACCESS_TOKEN = "common.access_token"
        private const val KEY_HEROW_ID = "common.herow_id"
        private const val KEY_SDK_KEY = "common.sdk_key"
        private const val KEY_DEVICE_ID = "common.device_id"
        private const val KEY_CUSTOM_ID = "common.custom_id"
        private const val KEY_CUSTOM_PREPROD_URL = "common.custom_preprod_url"
        private const val KEY_CUSTOM_PROD_URL = "common.custom_prod_url"
        private const val KEY_PLATFORM_NAME = "common.platform_name"
        private const val KEY_ADVERTISER_ID = "detection.ad_id"
        private const val KEY_TOKEN_TIMEOUT = "common.timeout_token"
        private const val KEY_USER_INFO = "common.user_info"
        private const val KEY_LAST_USER_INFO_LAUNCH = "common.last_user_info_launch"
        private const val KEY_CACHE_TIMEOUT = "common.timeout_cache"
        private const val KEY_UPDATE_CACHE = "common_update_cache"
        private const val KEY_LAST_LAUNCH_CACHE = "common_last_cache_request"
        private const val KEY_SAVED_GEOHASH = "common.saved_geohash"
        private const val KEY_REPEAT_INTERVAL = "common.repeat_interval"
        private const val KEY_OPTIN = "common.optin"
        private const val KEY_SDK = "common.sdk"
        private const val KEY_CLICK_AND_COLLECT_PROGRESS = "detection.click_and_collect_progress"
        private const val KEY_HEROW_CAPPING = "detection.herow_capping"
        private const val KEY_HEROW_CAPPINGS = "detection.herow_cappings"
        private const val KEY_HEROW_CONFIG = "detection.config_object"
        private const val KEY_LAUNCH_CONFIG = "detection.config_request"
        private const val KEY_SAVED_PREVIOUS_ZONES = "detection.previous_zones"
        private const val KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION =
            "detection.previous_zones_for_notification"
        private const val ZONES_PREDICTIONS = "livemoments.zones_predictions"
        private const val TAGS_PREDICTIONS = "livemoments.tags_predictions"
        private const val PREDICTIONS = "livemoments.predictions"
    }

    fun getAll(): Int = dataHolder.getAll()

    fun getAllElements(): MutableMap<String, *> = dataHolder.getAllElements()

    fun getDeviceId(): String = dataHolder[KEY_DEVICE_ID]

    fun saveDeviceId(deviceId: String) {
        if (deviceId.isNotEmpty()) {
            dataHolder[KEY_DEVICE_ID] = deviceId
        }
    }

    fun saveConfig(config: ConfigResult) {
        val configToString = GsonProvider.toJson(config, ConfigResult::class.java)
        dataHolder[KEY_HEROW_CONFIG] = configToString
    }

    fun getConfig(): ConfigResult? {
        val string: String = dataHolder[KEY_HEROW_CONFIG]
        if (string.isNotEmpty()) {
            return GsonProvider.fromJson(string, ConfigResult::class.java)
        }
        return null
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

    fun removeAccessToken() {
        if (dataHolder.containsKey(KEY_ACCESS_TOKEN)) {
            dataHolder.removeKey(KEY_ACCESS_TOKEN)
        }
    }

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

    fun getSdkKey(): String = dataHolder[KEY_SDK_KEY]

    fun saveSdkKey(key: String) {
        if (key.isNotEmpty()) {
            dataHolder[KEY_SDK_KEY] = key
        }
    }

    fun getPlatformName(): HerowPlatform =
        GsonProvider.fromJson(dataHolder[KEY_PLATFORM_NAME], HerowPlatform::class.java)

    fun savePlatform(platform: HerowPlatform) {
        dataHolder[KEY_PLATFORM_NAME] = Gson().toJson(platform)
    }

    fun getCustomID(): String =
        if (dataHolder.containsKey(KEY_CUSTOM_ID)) {
            dataHolder[KEY_CUSTOM_ID]
        } else {
            ""
        }

    fun removeCustomID() {
        dataHolder[KEY_CUSTOM_ID] = ""
    }

    fun saveCustomID(customID: String) {
        if (customID.isNotEmpty()) {
            dataHolder[KEY_CUSTOM_ID] = customID
        }
    }

    fun saveCustomPreProdURL(preProdURL: String) {
        dataHolder[KEY_CUSTOM_PREPROD_URL] = preProdURL
    }

    fun getCustomPreProdURL(): String =
        if (!dataHolder.containsKey(KEY_CUSTOM_PREPROD_URL)) {
            Constants.DEFAULT_PRE_PROD_URL
        } else {
            if (dataHolder[KEY_CUSTOM_PREPROD_URL, ""].isBlank() || dataHolder[KEY_CUSTOM_PREPROD_URL, ""].isEmpty()) {
                Constants.DEFAULT_PRE_PROD_URL
            } else {
                dataHolder[KEY_CUSTOM_PREPROD_URL]
            }
        }


    fun getCustomProdURL(): String =
        if (!dataHolder.containsKey(KEY_CUSTOM_PROD_URL)) {
            Constants.DEFAULT_PROD_URL
        } else {
            if (dataHolder[KEY_CUSTOM_PROD_URL, ""].isBlank() || dataHolder[KEY_CUSTOM_PROD_URL, ""].isEmpty()) {
                Constants.DEFAULT_PROD_URL
            } else {
                dataHolder[KEY_CUSTOM_PROD_URL]
            }
        }

    fun saveCustomProdURL(prodURL: String) {
        print("Saving prodURL: $prodURL")
        dataHolder[KEY_CUSTOM_PROD_URL] = prodURL
    }

    fun getCurrentURL(): String = when (getPlatformName()) {
        HerowPlatform.PRE_PROD -> if (dataHolder.containsKey(KEY_CUSTOM_PREPROD_URL)) {
            dataHolder[KEY_CUSTOM_PREPROD_URL]
        } else {
            ""
        }
        HerowPlatform.PROD -> if (dataHolder.containsKey(KEY_CUSTOM_PROD_URL)) {
            dataHolder[KEY_CUSTOM_PROD_URL]
        } else {
            ""
        }
        else -> ""
    }

    fun removeCustomURL() {
        when (getPlatformName()) {
            HerowPlatform.PROD -> if (dataHolder.containsKey(KEY_CUSTOM_PROD_URL)) {
                dataHolder[KEY_CUSTOM_PROD_URL] = ""
            }
            HerowPlatform.PRE_PROD -> if (dataHolder.containsKey(KEY_CUSTOM_PREPROD_URL)) {
                dataHolder[KEY_CUSTOM_PREPROD_URL] = ""
            }
            else -> println("Platform name not recognized")
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

    fun userInfoWasLaunched(): Boolean = dataHolder.containsKey(KEY_LAST_USER_INFO_LAUNCH)

    fun saveLastTimeUserInfoLaunch(timestamp: Long) {
        dataHolder[KEY_LAST_USER_INFO_LAUNCH] = timestamp
    }

    fun lastTimeUserInfoWasLaunched(): Long = dataHolder[KEY_LAST_USER_INFO_LAUNCH]

    fun loadSaveStringToUserInfo(): UserInfo {
        val savedUserInfo = dataHolder.get<String>(KEY_USER_INFO)
        return GsonProvider.fromJson(savedUserInfo, UserInfo::class.java)
    }

    fun saveModifiedCacheTime(date: String) {
        dataHolder[KEY_CACHE_TIMEOUT] = date
    }

    fun getLastSavedModifiedDateTimeCache(): String =
        if (!dataHolder.containsKey(KEY_CACHE_TIMEOUT)) {
            ""
        } else {
            dataHolder[KEY_CACHE_TIMEOUT]
        }

    fun updateCache(update: Boolean) {
        dataHolder[KEY_UPDATE_CACHE] = update
    }

    fun getUpdateCacheStatus(): Boolean {
        return dataHolder[KEY_UPDATE_CACHE]
    }

    fun hasNoCacheTimeSaved(): Boolean = !dataHolder.containsKey(KEY_CACHE_TIMEOUT)

    fun didSaveLastLaunchCache(): Boolean = dataHolder.containsKey(KEY_LAST_LAUNCH_CACHE)

    fun saveLastLaunchCacheRequest(timestamp: Long) {
        dataHolder[KEY_LAST_LAUNCH_CACHE] = timestamp
    }

    fun getLastTimeCacheWasLaunched(): Long {
        return if (didSaveLastLaunchCache()) {
            dataHolder[KEY_LAST_LAUNCH_CACHE]
        } else {
            0L
        }
    }

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

    fun removeSavedHerowCapping() = dataHolder.removeKey(KEY_HEROW_CAPPING)

    fun saveHerowCapping(campaignId: String, herowCapping: String) {
        val cappings = getHerowCappings()
        cappings[campaignId] = herowCapping
        val mapString = GsonProvider.toJson(cappings, HashMap::class.java)
        dataHolder[KEY_HEROW_CAPPINGS] = mapString
    }

    @Suppress("UNCHECKED_CAST")
    private fun getHerowCappings(): HashMap<String, String> {
        if (!dataHolder.containsKey(KEY_HEROW_CAPPINGS)) {
            val emptyMap = emptyMap<String, String>()
            val emptyString = GsonProvider.toJson(emptyMap, HashMap::class.java)
            dataHolder[KEY_HEROW_CAPPINGS] = emptyString
        }
        val map =
            GsonProvider.fromJson(
                dataHolder[KEY_HEROW_CAPPINGS],
                HashMap::class.java
            )
        GlobalLogger.shared.info(null, "All Cappings = $map")
        return map as HashMap<String, String>
    }

    fun getHerowCapping(campaign: Campaign): HerowCapping {
        val string = getHerowCappings()[campaign.id] ?: ""

        val result = if (string.isEmpty()) {
            HerowCapping().apply {
                count = 0
                razDate = TimeHelper.getCurrentTime()
                campaignId = campaign.id!!
            }
        } else {
            GsonProvider.fromJson(string, HerowCapping::class.java)
        }

        GlobalLogger.shared.info(null, "All Cappings = $result")
        return result
    }

    fun getHerowCapping(): HerowCapping =
        GsonProvider.fromJson(dataHolder[KEY_HEROW_CAPPING], HerowCapping::class.java)

    fun firstTimeLaunchingConfig(): Boolean = !dataHolder.containsKey(KEY_LAUNCH_CONFIG)

    fun saveConfigLaunch(time: Long) {
        dataHolder[KEY_LAUNCH_CONFIG] = time
    }

    fun getLastConfigLaunch(): Long = dataHolder[KEY_LAUNCH_CONFIG]

    fun hasPreviousZones(): Boolean = dataHolder.containsKey(KEY_SAVED_PREVIOUS_ZONES)

    fun hasPreviousZonesForNotification(): Boolean = dataHolder.containsKey(
        KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION
    )

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
        val savedPreviousZonesForNotification =
            dataHolder.get<String>(KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION)

        val listType: Type = object :
            TypeToken<ArrayList<Zone>?>() {}.type
        return Gson().fromJson(savedPreviousZonesForNotification, listType)
    }

    fun clearPreviousZones() = dataHolder.removeKey(KEY_SAVED_PREVIOUS_ZONES)

    fun clearPreviousZonesForNotification() = dataHolder.removeKey(
        KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION
    )

    fun reset() = dataHolder.removeAll()

    fun resetForCustomURL() {
        if (dataHolder.containsKey(KEY_ACCESS_TOKEN)) {
            dataHolder.removeKey(KEY_ACCESS_TOKEN)
        }

        if (dataHolder.containsKey(KEY_TOKEN_TIMEOUT)) {
            dataHolder.removeKey(KEY_TOKEN_TIMEOUT)
        }

        if (dataHolder.containsKey(KEY_USER_INFO)) {
            dataHolder.removeKey(KEY_USER_INFO)
        }

        if (dataHolder.containsKey(KEY_LAST_USER_INFO_LAUNCH)) {
            dataHolder.removeKey(KEY_LAST_USER_INFO_LAUNCH)
        }

        if (dataHolder.containsKey(KEY_LAST_LAUNCH_CACHE)) {
            dataHolder.removeKey(KEY_LAST_LAUNCH_CACHE)
        }

        if (dataHolder.containsKey(KEY_HEROW_CONFIG)) {
            dataHolder.removeKey(KEY_HEROW_CONFIG)
        }

        if (dataHolder.containsKey(KEY_LAUNCH_CONFIG)) {
            dataHolder.removeKey(KEY_LAUNCH_CONFIG)
        }

        if (dataHolder.containsKey(KEY_SAVED_PREVIOUS_ZONES)) {
            dataHolder.removeKey(KEY_SAVED_PREVIOUS_ZONES)
        }

        if (dataHolder.containsKey(KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION)) {
            dataHolder.removeKey(KEY_SAVED_PREVIOUS_ZONES_FOR_NOTIFICATION)
        }

        if (dataHolder.containsKey(PREDICTIONS)) {
            dataHolder.removeKey(PREDICTIONS)
        }
    }

    fun didPredict(predictions: ArrayList<Prediction>) {
        if (predictions.isNotEmpty()) {
            savePredictions(predictions)
        }
    }

    fun didZonePredict(predictions: ArrayList<ZonePrediction>) {
        if (predictions.isNotEmpty()) {
            saveZonesPredictions(predictions)
        }
    }

    fun didPredictionsForTags(predictions: ArrayList<TagPrediction>) {
        if (predictions.isNotEmpty()) {
            saveTagsPredictions(predictions)
        }
    }

    private fun saveZonesPredictions(zonesPrediction: ArrayList<ZonePrediction>) {
        dataHolder[ZONES_PREDICTIONS] = Gson().toJson(zonesPrediction)
    }

    fun getLastZonesPredictions(): ArrayList<ZonePrediction> =
        if (dataHolder.containsKey(ZONES_PREDICTIONS)) {
            val savedPreviousZonesPredictions = dataHolder.get<String>(ZONES_PREDICTIONS)

            val listType: Type = object :
                TypeToken<ArrayList<ZonePrediction>?>() {}.type
            Gson().fromJson(savedPreviousZonesPredictions, listType)
        } else {
            arrayListOf()
        }

    private fun saveTagsPredictions(tagsPredictions: ArrayList<TagPrediction>) {
        dataHolder[TAGS_PREDICTIONS] = Gson().toJson(tagsPredictions)
    }

    fun getLastTagsPredictions(): ArrayList<TagPrediction> =
        if (dataHolder.containsKey(TAGS_PREDICTIONS)) {
            val savedPreviousTagsPredictions = dataHolder.get<String>(TAGS_PREDICTIONS)

            val listType: Type = object :
                TypeToken<ArrayList<TagPrediction>?>() {}.type
            Gson().fromJson(savedPreviousTagsPredictions, listType)
        } else {
            arrayListOf()
        }

    private fun savePredictions(predictions: ArrayList<Prediction>) {
        dataHolder[PREDICTIONS] = Gson().toJson(predictions)
    }
}