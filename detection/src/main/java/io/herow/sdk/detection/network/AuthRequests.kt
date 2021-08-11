package io.herow.sdk.detection.network

import androidx.work.Data
import com.google.gson.Gson
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.RetrofitBuilder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.token.PlatformData
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import io.herow.sdk.connection.userinfo.UserInfoResult
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.network.model.RetrofitConnectionObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.inject

/**
 * Token and UserInfo workflow
 */
class AuthRequests(
    private val sessionHolder: SessionHolder,
    private val data: Data
) : ICustomKoinComponent {
    companion object {
        const val KEY_SDK_ID = "detection.sdk_id"
        const val KEY_SDK_KEY = "detection.sdk_key"
        const val KEY_CUSTOM_ID = "detection.custom_id"
        const val KEY_PLATFORM = "detection.platform"
    }

    private val dispatcher: CoroutineDispatcher by inject()

    private var isWorking = false
    private val platform = getPlatform()

    private val herowAPI: IHerowAPI = RetrofitBuilder.buildRetrofitForAPI(
        sessionHolder,
        getApiUrl(platform),
        IHerowAPI::class.java
    )

    /**
     * ConfigWorker needs an instance of HerowAPI
     */
    fun getHerowAPI(): IHerowAPI = herowAPI

    private suspend fun authenticationWorkFlow(request: suspend (herowAPI: IHerowAPI) -> Unit) {
        GlobalLogger.shared.info(null, "flow: authenticatoinWorkFlow")
        if (!isTokenUsable(sessionHolder)) {
            withContext(dispatcher) {
                launchTokenRequest(sessionHolder, platform, herowAPI, request)
            }
        } else {
            GlobalLogger.shared.info(null, "Token is usable or isWorking")
            request(herowAPI)
        }
    }

    private suspend fun userInfoWorkFlow(request: suspend (herowAPI: IHerowAPI) -> Unit) {
        GlobalLogger.shared.info(null, "flow :userInfoWorkFlow")

        if (needUserInfo(sessionHolder)) {
            launchUserInfoRequest(sessionHolder, herowAPI, request)
        } else {
            request(herowAPI)
        }
    }

    private fun needUserInfo(sessionHolder: SessionHolder): Boolean {
        if (sessionHolder.hasNoUserInfoSaved()) {
            GlobalLogger.shared.info(null, "User info has not been saved yet")
            return true
        }

        if (!isUserInfoUpToDate()) {
            GlobalLogger.shared.info(null, "User info is not up to date")
            return true
        }

        if (sessionHolder.userInfoWasLaunched()) {
            val lastUserInfoLaunch = sessionHolder.lastTimeUserInfoWasLaunched()

            GlobalLogger.shared.info(null, "User Info last launch is: $lastUserInfoLaunch")

            if (lastUserInfoLaunch + 86400000 < TimeHelper.getCurrentTime()) {
                GlobalLogger.shared.info(null, "User info has not been updated in 24 hours")
                return true
            }
        }

        return false
    }

    fun getUserInfoIfNeeded() {
        runBlocking {
            withContext(dispatcher) {
                authenticationWorkFlow {
                    userInfoWorkFlow {}
                }
            }
        }
    }

    suspend fun execute(request: suspend (herowAPI: IHerowAPI) -> Unit = {}) {
        authenticationWorkFlow {
            userInfoWorkFlow {
                request(herowAPI)
            }
        }
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
        return when (platform) {
            HerowPlatform.PRE_PROD -> {
                IHerowAPI.PRE_PROD_BASE_URL
            }
            HerowPlatform.TEST -> {
                IHerowAPI.TEST_BASE_URL
            }
            else -> {
                IHerowAPI.PROD_BASE_URL
            }
        }
    }

    /**
     * Check if token is usable
     */
    private fun isTokenUsable(sessionHolder: SessionHolder): Boolean {
        GlobalLogger.shared.info(
            null,
            "AccessToken is not empty = ${sessionHolder.getAccessToken().isNotEmpty()}"
        )
        GlobalLogger.shared.info(
            null,
            "AccessToken is valid = ${isTokenValid(sessionHolder.getTimeOutTime())}"
        )
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
        herowAPI: IHerowAPI,
        request: suspend (herowAPI: IHerowAPI) -> Unit
    ) {
        isWorking = true
        val sdkId = data.getString(KEY_SDK_ID) ?: ""
        val sdkKey = data.getString(KEY_SDK_KEY) ?: ""
        GlobalLogger.shared.info(null, "AuthRequest isWorking = $isWorking")
        GlobalLogger.shared.info(null, "SdkID is $sdkId")
        GlobalLogger.shared.info(null, "SdkKey is $sdkKey")

        if (sdkId.isNotEmpty() && sdkKey.isNotEmpty()) {
            val platformData = PlatformData(platform)

            val retrofitObject = RetrofitConnectionObject(
                sdkId,
                sdkKey,
                platformData.clientId,
                platformData.clientSecret,
                platformData.redirectUri
            )

            val retrofitObjectString =
                Gson().toJson(retrofitObject, RetrofitConnectionObject::class.java)
            val tokenResponse = herowAPI.token(retrofitObjectString)
            isWorking = false
            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let { tokenResult: TokenResult ->
                    sessionHolder.saveAccessToken(tokenResult.getToken())
                    sessionHolder.saveTimeOutTime(tokenResult.getTimeoutTime())
                    GlobalLogger.shared.info(
                        null,
                        "SavedTimeOutTime = ${tokenResult.getTimeoutTime()}"
                    )
                    request(herowAPI)
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

    suspend fun launchUserInfoRequest(
        sessionHolder: SessionHolder,
        herowAPI: IHerowAPI,
        request: suspend (herowAPI: IHerowAPI) -> Unit = {}
    ) {
        val userInfo = getCurrentUserInfo()
        val jsonString = GsonProvider.toJson(userInfo, UserInfo::class.java)
        sessionHolder.saveStringUserInfo(jsonString)

        GlobalLogger.shared.info(null, "UserInfo string is $jsonString")
        val userInfoResponse = herowAPI.userInfo(jsonString)
        GlobalLogger.shared.info(null, "UserInfoResponse is $userInfoResponse")

        if (userInfoResponse.isSuccessful) {
            sessionHolder.saveLastTimeUserInfoLaunch(TimeHelper.getCurrentTime())
            userInfoResponse.body()?.let { userInfoResult: UserInfoResult ->
                sessionHolder.saveHerowId(userInfoResult.herowId)
                GlobalLogger.shared.info(null, "UserInfoResponse is successful")
                request(herowAPI)
            }
        }
    }
}