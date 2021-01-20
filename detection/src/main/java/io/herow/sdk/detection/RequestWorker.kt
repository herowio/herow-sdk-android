package io.herow.sdk.detection

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.IdentifiersHolder
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

class RequestWorker(context: Context,
                    workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_SDK_ID     = "detection.sdk_id"
        const val KEY_SDK_KEY    = "detection.sdk_key"
        const val KEY_CUSTOM_ID  = "detection.custom_id"
        const val KEY_PLATFORM   = "detection.platform"
    }

    override suspend fun doWork(): Result {
        val identifiersHolder = IdentifiersHolder(DataHolder(applicationContext))
        val platform = getPlatform()
        val herowAPI: HerowAPI = RetrofitBuilder.buildRetrofitForAPI(identifiersHolder, getApiUrl(platform), HerowAPI::class.java)
        launchTokenRequest(identifiersHolder, platform, herowAPI)
        launchUserInfoRequest(identifiersHolder, herowAPI)
        launchConfigRequest(identifiersHolder, herowAPI)
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

    private suspend fun launchTokenRequest(identifiersHolder: IdentifiersHolder,
                                           platform: HerowPlatform,
                                           herowAPI: HerowAPI) {
        val sdkId = inputData.getString(KEY_SDK_ID) ?: ""
        val sdkKey = inputData.getString(KEY_SDK_KEY) ?: ""
        if (sdkId.isNotEmpty() && sdkKey.isNotEmpty()) {
            val platformData = PlatformData(platform)
            val tokenResponse = herowAPI.token(sdkId, sdkKey, platformData.clientId, platformData.clientSecret, platformData.redirectUri)
            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let { tokenResult: TokenResult ->
                    identifiersHolder.saveAccessToken(tokenResult.getToken())
                }
            }
        }
    }

    private suspend fun launchUserInfoRequest(identifiersHolder: IdentifiersHolder, herowAPI: HerowAPI) {
        val customId = inputData.getString(KEY_CUSTOM_ID) ?: ""
        val userInfo = UserInfo(
            listOf(Optin("USER_DATA", true)),
            identifiersHolder.getAdvertiserId(), customId,
            DeviceHelper.getDefaultLanguage(), TimeHelper.getUtcOffset()
        )

        val moshi = Moshi.Builder().build()
        val jsonAdapter: JsonAdapter<UserInfo> = moshi.adapter(UserInfo::class.java)
        val jsonString = jsonAdapter.toJson(userInfo)

        val userInfoResponse = herowAPI.userInfo(jsonString)
        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let { userInfoResult: UserInfoResult ->
                identifiersHolder.saveHerowId(userInfoResult.herowId)
            }
        }
    }

    private suspend fun launchConfigRequest(identifiersHolder: IdentifiersHolder, herowAPI: HerowAPI) {
        val configResponse = herowAPI.config()
        if (configResponse.isSuccessful) {
            configResponse.body()?.let { configResult: ConfigResult ->
                println(configResult)
                val lastTimeCacheWasModified = configResponse.headers()[HerowHeaders.LAST_TIME_CACHE_MODIFIED]
                println(lastTimeCacheWasModified)
            }
        }
    }
}