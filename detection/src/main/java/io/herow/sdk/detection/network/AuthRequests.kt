package io.herow.sdk.detection.network

import android.content.Context
import androidx.annotation.Keep
import androidx.work.Data
import com.google.gson.Gson
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.json.GsonProvider
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.IHerowAPI
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.token.PlatformData
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.Optin
import io.herow.sdk.connection.userinfo.UserInfo
import io.herow.sdk.connection.userinfo.UserInfoResult
import io.herow.sdk.detection.helpers.PermissionLocationHelper
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.network.model.RetrofitConnectionObject
import io.herow.sdk.detection.session.RetrofitBuilder
import org.koin.core.component.inject
import java.net.MalformedURLException

/**
 * Token and UserInfo workflow
 */
@Keep
class AuthRequests(
    private val data: Data
) : ICustomKoinComponent {

    companion object {
        const val KEY_SDK_ID = "detection.sdk_id"
        const val KEY_SDK_KEY = "detection.sdk_key"
        const val KEY_CUSTOM_ID = "detection.custom_id"
        const val KEY_PLATFORM = "detection.platform"
    }

    private val sessionHolder: SessionHolder by inject()
    private val context: Context by inject()
    private var isWorking = false
    private val platform = getPlatform()

    private val herowAPI: IHerowAPI = RetrofitBuilder.buildRetrofitForAPI(
        getApiUrl(platform),
        IHerowAPI::class.java
    )

    /**
     * ConfigWorker needs an instance of HerowAPI
     */
    fun getHerowAPI(): IHerowAPI = herowAPI

    private suspend fun authenticationWorkFlow(request: suspend (herowAPI: IHerowAPI) -> Unit) {
        GlobalLogger.shared.info(null, "flow: authenticationWorkFlow")
        if (!isTokenUsable()) {
            launchTokenRequest(platform, herowAPI, request)
        } else {
            GlobalLogger.shared.info(null, "Token is usable or isWorking")
            try {
                request(herowAPI)
            } catch (exception: Throwable) {
                println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
            }
        }
    }

    private suspend fun userInfoWorkFlow(request: suspend (herowAPI: IHerowAPI) -> Unit) {
        GlobalLogger.shared.info(null, "flow :userInfoWorkFlow")

        if (needUserInfo()) {
            launchUserInfoRequest(herowAPI, request)
        } else {
            try {
                request(herowAPI)
            } catch (exception: Throwable) {
                println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
            }
        }
    }

    private fun needUserInfo(): Boolean {
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

    suspend fun getUserInfoIfNeeded() {
        authenticationWorkFlow {
            userInfoWorkFlow {}
        }
    }

    suspend fun execute(request: suspend (herowAPI: IHerowAPI) -> Unit = {}) {
        authenticationWorkFlow {
            userInfoWorkFlow {
                try {
                    request(herowAPI)
                } catch (exception: Throwable) {
                    println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
                }
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
                sessionHolder.getCustomPreProdURL()
            }
            HerowPlatform.TEST -> {
                IHerowAPI.TEST_BASE_URL
            }
            else -> {
                sessionHolder.getCustomProdURL()
            }
        }
    }

    /**
     * Check if token is usable
     */
    private fun isTokenUsable(): Boolean {
        GlobalLogger.shared.info(
            null,
            "AccessToken is not empty = ${sessionHolder.getAccessToken().isNotEmpty()}"
        )
        GlobalLogger.shared.info(
            null,
            "AccessToken is not empty = ${sessionHolder.getAccessToken().isNotEmpty()}"
        )
        GlobalLogger.shared.info(null, "AccessToken is valid = ${isTokenValid(sessionHolder.getTimeOutTime())}")
        return (sessionHolder.getAccessToken().isNotEmpty()
                && isTokenValid(sessionHolder.getTimeOutTime()))
    }


    /**
     * Check if token time is still valid
     */
    private fun isTokenValid(recordedTime: Long): Boolean =
        recordedTime > TimeHelper.getCurrentTime()

    private suspend fun launchTokenRequest(
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

            GlobalLogger.shared.info(
                context = null,
                "launchTokenRequest - retrofitObject: $retrofitObjectString"
            )
            GlobalLogger.shared.info(context = null, "launchTokenRequest - $tokenResponse")

            if (tokenResponse.isSuccessful) {
                tokenResponse.body()?.let { tokenResult: TokenResult ->
                    sessionHolder.saveAccessToken(tokenResult.getToken())
                    sessionHolder.saveTimeOutTime(tokenResult.getTimeoutTime())
                    GlobalLogger.shared.info(
                        null,
                        "SavedTimeOutTime = ${tokenResult.getTimeoutTime()}"
                    )
                    try {
                        request(herowAPI)
                    } catch (exception: Throwable) {
                        println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
                    }
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
            customId = data.getString(KEY_CUSTOM_ID) ?: "",
            permissionLocation = PermissionLocationHelper.treatActualPermissions(context)
        )
    }

    private fun getSavedUserInfo(): UserInfo = sessionHolder.loadSaveStringToUserInfo()

    private fun isUserInfoUpToDate(): Boolean = getCurrentUserInfo() == getSavedUserInfo()

    suspend fun launchUserInfoRequest(
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
                try {
                    request(herowAPI)
                } catch (exception: Throwable) {
                    println("YYY - Exception in URL, cause is: ${exception.cause} - ${exception.message}")
                }
            }
        }
    }
}