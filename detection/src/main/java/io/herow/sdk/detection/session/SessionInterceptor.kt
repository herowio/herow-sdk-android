package io.herow.sdk.detection.session

import androidx.annotation.Keep
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.HerowHeaders
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.BuildConfig
import io.herow.sdk.detection.koin.ICustomKoinComponent
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.component.inject

/**
 * Allow us to add necessary headers to request as soon as we have saved them in SharedPreferences
 * using an OkHttp Interceptor.
 * @see Interceptor
 * @see DataHolder
 */
@Keep
class SessionInterceptor: Interceptor, ICustomKoinComponent {

    private val sessionHolder: SessionHolder by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader(HerowHeaders.SDK_VERSION_HEADER, BuildConfig.SDK_VERSION)
            val sdkID = sessionHolder.getSDKID()

            if (sdkID.isNotEmpty()) {
                requestBuilder.addHeader(HerowHeaders.SDK_HEADER, sdkID)
            }

            val accessToken = sessionHolder.getAccessToken()
            if (accessToken.isNotEmpty()) {
                requestBuilder.addHeader(HerowHeaders.AUTHORIZATION_HEADER, accessToken)
            }

            val herowId = sessionHolder.getHerowId()
            if (herowId.isNotEmpty()) {
                requestBuilder.addHeader(HerowHeaders.HEROW_ID_HEADER, herowId)
            }

            val deviceId = sessionHolder.getDeviceId()
            if (deviceId.isNotEmpty()) {
                requestBuilder.addHeader(HerowHeaders.DEVICE_ID_HEADER, deviceId)
            }

            return chain.proceed(requestBuilder.build())
        } catch ( e: Throwable) {
                return Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(400)
                    .message("Something went wrong")
                    .body("Something went wrong".toResponseBody(null))
                    .build()
        }
    }
}