package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowHeaders
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.token.PlatformData
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import io.herow.sdk.connection.userinfo.UserInfoResult
import io.herow.sdk.detection.HerowInitializer

/**
 * @see HerowAPI#config()
 */
class ConfigWorker(context: Context,
                   workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_SDK_ID     = "detection.sdk_id"
        const val KEY_SDK_KEY    = "detection.sdk_key"
        const val KEY_CUSTOM_ID  = "detection.custom_id"
        const val KEY_PLATFORM   = "detection.platform"
    }

    override suspend fun doWork(): Result {
        val sessionHolder = SessionHolder(DataHolder(applicationContext))
        val platform = getPlatform()
        val herowAPI: HerowAPI = RetrofitBuilder.buildRetrofitForAPI(sessionHolder, getApiUrl(platform), HerowAPI::class.java)
        launchTokenRequest(sessionHolder, platform, herowAPI)
        launchUserInfoRequest(sessionHolder, herowAPI)
        launchConfigRequest(sessionHolder, herowAPI)
        return Result.success()
    }

    private fun getPlatform(): HerowPlatform {
        val platformURLString = inputData.getString(KEY_PLATFORM) ?: ""
        if (platformURLString.isNotEmpty()) {
            if (HerowPlatform.PRE_PROD == HerowPlatform.valueOf(platformURLString)) {
                return HerowPlatform.PRE_PROD
            }
        }
        return HerowPlatform.PROD
    }

    private fun getApiUrl(platform: HerowPlatform): String {
        if (platform == HerowPlatform.PRE_PROD) {
            return HerowAPI.PRE_PROD_BASE_URL
        }
        return HerowAPI.PROD_BASE_URL
    }

    private suspend fun launchTokenRequest(sessionHolder: SessionHolder,
                                           platform: HerowPlatform,
                                           herowAPI: HerowAPI) {
        val sdkId = inputData.getString(KEY_SDK_ID) ?: ""
        val sdkKey = inputData.getString(KEY_SDK_KEY) ?: ""
        if (sdkId.isNotEmpty() && sdkKey.isNotEmpty()) {
            val platformData = PlatformData(platform)
            val tokenResponse = herowAPI.token(sdkId, sdkKey, platformData.clientId, platformData.clientSecret, platformData.redirectUri)
            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let { tokenResult: TokenResult ->
                    sessionHolder.saveAccessToken(tokenResult.getToken())
                }
            }
        }
    }

    private suspend fun launchUserInfoRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        val customId = inputData.getString(KEY_CUSTOM_ID) ?: ""
        val userInfo = UserInfo(
            listOf(Optin("USER_DATA", true)),
            sessionHolder.getAdvertiserId(), customId, TimeHelper.getUtcOffset()
        )

        val jsonString = Gson().toJson(userInfo)

        val userInfoResponse = herowAPI.userInfo(jsonString)
        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let { userInfoResult: UserInfoResult ->
                sessionHolder.saveHerowId(userInfoResult.herowId)
            }
        }
    }

    private suspend fun launchConfigRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        val configResponse = herowAPI.config()
        if (configResponse.isSuccessful) {
            configResponse.body()?.let { configResult: ConfigResult ->
                if (configResult.isGeofenceEnable) {
                    HerowInitializer.launchGeofencingMonitoring()
                }
                val lastTimeCacheWasModified = configResponse.headers()[HerowHeaders.LAST_TIME_CACHE_MODIFIED]
                println(lastTimeCacheWasModified)
            }
        }
    }
}