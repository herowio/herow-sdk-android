package io.herow.sdk.detection.network

import androidx.work.Data
import com.google.gson.Gson
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowAPI
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.token.PlatformData
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import io.herow.sdk.connection.userinfo.UserInfoResult
import io.herow.sdk.detection.network.model.RetrofitConnectionObject
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
        GlobalLogger.shared.info(null,"Beginning of execute method in AuthRequests")
        GlobalLogger.shared.info(null, "Token, before isTokenUsable, is: ${sessionHolder.getAccessToken()}")

        if (!isTokenUsable(sessionHolder)) {
            GlobalLogger.shared.info(null,"Token is not usable")

            withContext(Dispatchers.IO) {
                launchTokenRequest(sessionHolder, platform, herowAPI)
            }
        } else {
            GlobalLogger.shared.info(null,"Token is usable")
        }

        GlobalLogger.shared.info(null,"Token, after isTokenUsable, is: ${sessionHolder.getAccessToken()}")
        GlobalLogger.shared.info(null,"GetTimeOutTime value is: ${sessionHolder.getTimeOutTime()}")

        if (sessionHolder.hasNoUserInfoSaved() || !isUserInfoUpToDate()) {
            GlobalLogger.shared.info(null,"Launching userInfoRequest method")
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
            } else if (HerowPlatform.TEST == HerowPlatform.valueOf(platformURLString)) {
                return HerowPlatform.TEST
            }
        }
        return HerowPlatform.PROD
    }

    private fun getApiUrl(platform: HerowPlatform): String {
        if (platform == HerowPlatform.PRE_PROD) {
            return HerowAPI.PRE_PROD_BASE_URL
        } else if (platform == HerowPlatform.TEST) {
            return HerowAPI.TEST_BASE_URL
        } else {
            return HerowAPI.PROD_BASE_URL
        }
    }

    /**
     * Check if token is usable
     */
    private fun isTokenUsable(sessionHolder: SessionHolder): Boolean {
        GlobalLogger.shared.info(null,"AccessToken is not empty = ${sessionHolder.getAccessToken().isNotEmpty()}")
        GlobalLogger.shared.info(null,"AccessToken is valid = ${isTokenValid(sessionHolder.getTimeOutTime())}")
        return (sessionHolder.getAccessToken().isNotEmpty()
                && isTokenValid(sessionHolder.getTimeOutTime()))
    }


    /**
     * Check if token time is still valid
     */
    private fun isTokenValid(recordedTime: Long): Boolean =
        recordedTime > TimeHelper.getCurrentTime()

    private suspend fun launchTokenRequest(
        sessionHolder: SessionHolder,
        platform: HerowPlatform,
        herowAPI: HerowAPI
    ) {
        val sdkId = data.getString(KEY_SDK_ID) ?: ""
        val sdkKey = data.getString(KEY_SDK_KEY) ?: ""

        GlobalLogger.shared.info(null,"SdkID is $sdkId")
        GlobalLogger.shared.info(null,"SdkKey is $sdkKey")

        if (sdkId.isNotEmpty() && sdkKey.isNotEmpty()) {
            val platformData = PlatformData(platform)

            val retrofitObject = RetrofitConnectionObject(
                sdkId,
                sdkKey,
                platformData.clientId,
                platformData.clientSecret,
                platformData.redirectUri
            )

            val retrofitObjectString = Gson().toJson(retrofitObject, RetrofitConnectionObject::class.java)
            val tokenResponse = herowAPI.token(retrofitObjectString)

            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let { tokenResult: TokenResult ->
                    sessionHolder.saveAccessToken(tokenResult.getToken())
                    sessionHolder.saveTimeOutTime(tokenResult.getTimeoutTime())
                    GlobalLogger.shared.info(null,"SavedTimeOutTime = ${tokenResult.getTimeoutTime()}")
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

        GlobalLogger.shared.info(null,"UserInfo string is $jsonString")
        val userInfoResponse = herowAPI.userInfo(jsonString)
        GlobalLogger.shared.info(null,"UserInfoResponse is $userInfoResponse")

        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let { userInfoResult: UserInfoResult ->
                sessionHolder.saveHerowId(userInfoResult.herowId)

                GlobalLogger.shared.info(null,"UserInfoResponse is successful")
            }
        }
    }
}