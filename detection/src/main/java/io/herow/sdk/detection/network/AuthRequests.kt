package io.herow.sdk.detection.network

import android.util.Log
import androidx.work.Data
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.token.PlatformData
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import io.herow.sdk.connection.userinfo.UserInfoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Token and UserInfo workflow
 */
class AuthRequests(
    private val sessionHolder: SessionHolder,
    private val data: Data
) {
    companion object {
        const val KEY_SDK_ID = "detection.sdk_id"
        const val KEY_SDK_KEY = "detection.sdk_key"
        const val KEY_CUSTOM_ID = "detection.custom_id"
        const val KEY_PLATFORM = "detection.platform"
    }

    private val platform = getPlatform()
    private val herowAPI: HerowAPI = RetrofitBuilder.buildRetrofitForAPI(
        sessionHolder,
        getApiUrl(platform),
        HerowAPI::class.java
    )

    /**
     * ConfigWorker needs an instance of HerowAPI
     */
    fun getHerowAPI(): HerowAPI = herowAPI

    /**
     * Reusable execute method in any worker that needs a token
     */
    suspend fun execute(request: suspend (herowAPI: HerowAPI) -> Unit) {
        Log.i("XXX/EVENT", "AuthRequests - Beginning of execute method in AuthRequests")

        Log.i("XXX/EVENT", "AuthRequests - Is token empty?: ${sessionHolder.getAccessToken()}")

        val timeout: Boolean = sessionHolder.getTimeOutToken() < TimeHelper.getCurrentTime()

        Log.i("XXX/EVENT", "AuthRequests - Saved timeOut: ${sessionHolder.getTimeOutToken()}")
        Log.i("XXX/EVENT", "AuthRequests - CurrentTime: ${TimeHelper.getCurrentTime()}")
        Log.i("XXX/EVENT", "AuthRequests - Is token timeout?: $timeout")
        Log.i("XXX/EVENT", "AuthRequests - Is token usable?: ${isTokenUsable(sessionHolder)}")

        if (!isTokenUsable(sessionHolder)) {
            Log.i("XXX/EVENT", "AuthRequests - Token is not usable")
            withContext(Dispatchers.IO) {
                launchTokenRequest(sessionHolder, platform, herowAPI)
            }
        }

        if (sessionHolder.hasNoUserInfoSaved() || !isUserInfoUpToDate()) {
            Log.i("XXX/EVENT", "AuthRequests - Launching userInfoRequest")
            withContext(Dispatchers.IO) {
                launchUserInfoRequest(sessionHolder, herowAPI)
            }
        }

        request(herowAPI)
    }

    private fun getPlatform(): HerowPlatform {
        val platformURLString = data.getString(KEY_PLATFORM) ?: ""
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

    /**
     * Check if token is usable
     */
    private fun isTokenUsable(sessionHolder: SessionHolder): Boolean =
        (sessionHolder.getAccessToken().isNotEmpty()
                && !isTokenExpired(sessionHolder.getTimeOutToken()))

    /**
     * Check if token time is still valid
     */
    private fun isTokenExpired(timeoutTime: Long): Boolean =
        timeoutTime < TimeHelper.getCurrentTime()

    private suspend fun launchTokenRequest(
        sessionHolder: SessionHolder,
        platform: HerowPlatform,
        herowAPI: HerowAPI
    ) {
        val sdkId = data.getString(KEY_SDK_ID) ?: ""
        val sdkKey = data.getString(KEY_SDK_KEY) ?: ""

        Log.i("XXX/EVENT", "AuthRequests - sdkID is $sdkId")
        Log.i("XXX/EVENT", "AuthRequests - sdkKey is $sdkKey")

        if (sdkId.isNotEmpty() && sdkKey.isNotEmpty()) {
            val platformData = PlatformData(platform)
            val tokenResponse = herowAPI.token(
                sdkId,
                sdkKey,
                platformData.clientId,
                platformData.clientSecret,
                platformData.redirectUri
            )

            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let { tokenResult: TokenResult ->
                    sessionHolder.saveAccessToken(tokenResult.getToken())
                    sessionHolder.saveTimeBeforeTimeOut(tokenResult.getTimeoutTime(TimeHelper.getCurrentTime()))
                }
            }
        }
    }

    private fun getCurrentUserInfo(): UserInfo {
        val adID = if (sessionHolder.getAdvertiserId().isNullOrEmpty()) {
            null
        } else {
            sessionHolder.getAdvertiserId()
        }

        return UserInfo(
            optins = arrayListOf(Optin(value = sessionHolder.getOptinValue())),
            advertiserId = adID,
            customId = data.getString(KEY_CUSTOM_ID) ?: ""
        )
    }

    private fun getSavedUserInfo(): UserInfo = sessionHolder.loadSaveStringToUserInfo()

    private fun isUserInfoUpToDate(): Boolean = getCurrentUserInfo() == getSavedUserInfo()

    suspend fun launchUserInfoRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        val userInfo = getCurrentUserInfo()
        val jsonString = GsonProvider.toJson(userInfo, UserInfo::class.java)
        sessionHolder.saveStringUserInfo(jsonString)

        Log.i("XXX/EVENT", "AuthRequests - UserInfo string is $jsonString")
        val userInfoResponse = herowAPI.userInfo(jsonString)
        Log.i("XXX/EVENT", "AuthRequests - UserInfoResponse is $userInfoResponse")

        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let { userInfoResult: UserInfoResult ->
                sessionHolder.saveHerowId(userInfoResult.herowId)
                Log.i("XXX/EVENT", "AuthRequests - UserInfoResponse is successful")
            }
        }
    }
}