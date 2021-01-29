package io.herow.sdk.detection.network

import androidx.work.Data
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.token.PlatformData
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import io.herow.sdk.connection.userinfo.UserInfoResult
import io.herow.sdk.common.json.GsonProvider

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
     * Reusable execute method in any worker that needs a token
     */
    suspend fun execute(request: suspend (herowAPI: HerowAPI) -> Unit) {
        if (!isTokenUsable(sessionHolder)) {
            launchTokenRequest(sessionHolder, platform, herowAPI)
        }

        if (sessionHolder.isFirstInstance() || !isUserInfoUpToDate()) {
            launchUserInfoRequest(sessionHolder, herowAPI)
        }

        request(herowAPI)
    }

    /**
     * Check if token is usable
     */
    private fun isTokenUsable(sessionHolder: SessionHolder): Boolean {
        return (sessionHolder.getAccessToken()
            .isNotEmpty() || !isTokenExpired(sessionHolder.getTimeOutToken())
                )
    }

    /**
     * Check if token time is still valid
     */
    private fun isTokenExpired(timeoutTime: Long): Boolean {
        return (timeoutTime < TimeHelper.getCurrentTime())
    }

    private suspend fun launchUserInfoRequest(sessionHolder: SessionHolder, herowAPI: HerowAPI) {
        val jsonString = GsonProvider.toJson(getCurrentUserInfo())
        sessionHolder.jsonToStringUserInfo(jsonString)

        val userInfoResponse = herowAPI.userInfo(jsonString)

        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let { userInfoResult: UserInfoResult ->
                sessionHolder.saveHerowId(userInfoResult.herowId)
            }
        }
    }

    private suspend fun launchTokenRequest(
        sessionHolder: SessionHolder,
        platform: HerowPlatform,
        herowAPI: HerowAPI
    ) {
        val sdkId = data.getString(KEY_SDK_ID) ?: ""
        val sdkKey = data.getString(KEY_SDK_KEY) ?: ""

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

    @JvmName("getPlatform1")
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
     * ConfigWorker needs an instance of HerowAPI
     */
    fun getHerowAPI(): HerowAPI = herowAPI

    private fun getCurrentUserInfo(): UserInfo {
        val customId = data.getString(KEY_CUSTOM_ID) ?: ""

        return UserInfo(
            listOf(Optin("USER_DATA", true)),
            sessionHolder.getAdvertiserId(), customId, TimeHelper.getUtcOffset()
        )
    }

    private fun getSavedUserInfo(): UserInfo? = sessionHolder.stringToJsonUserInfo()

    private fun isUserInfoUpToDate(): Boolean = getSavedUserInfo() == getCurrentUserInfo()
}