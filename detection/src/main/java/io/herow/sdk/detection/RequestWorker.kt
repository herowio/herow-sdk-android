package io.herow.sdk.detection

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.token.PlatformData
import io.herow.sdk.connection.token.TokenResult

class RequestWorker(context: Context,
                    workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {
    companion object {
        const val KEY_SDK_ID   = "detection.sdk_id"
        const val KEY_SDK_KEY  = "detection.sdk_key"
        const val KEY_PLATFORM = "detection.platform"
    }
    override suspend fun doWork(): Result {
        val dataHolder = DataHolder(applicationContext)
        val platform = getPlatform()
        val herowAPI: HerowAPI = RetrofitBuilder.buildRetrofitForAPI(dataHolder, getApiUrl(platform), HerowAPI::class.java)
        launchTokenRequest(dataHolder, platform, herowAPI)
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

    private suspend fun launchTokenRequest(dataHolder: DataHolder,
                                           platform: HerowPlatform,
                                           herowAPI: HerowAPI) {
        val sdkId = inputData.getString(KEY_SDK_ID) ?: ""
        val sdkKey = inputData.getString(KEY_SDK_KEY) ?: ""
        if (sdkId.isNotEmpty() && sdkKey.isNotEmpty()) {
            val platformData = PlatformData(platform)
            val tokenResponse = herowAPI.token(sdkId, sdkKey, platformData.clientId, platformData.clientSecret, platformData.redirectUri)
            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let { tokenResult: TokenResult ->
                    dataHolder["connection.access_token"] = tokenResult.getToken()
                }
            }
        }
    }
}