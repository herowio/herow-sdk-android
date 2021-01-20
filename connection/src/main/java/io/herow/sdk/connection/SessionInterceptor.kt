package io.herow.sdk.connection

import io.herow.sdk.common.DataHolder
import io.herow.sdk.common.IdentifiersHolder
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Allow us to add necessary headers to request as soon as we have saved them in SharedPreferences
 * using an OkHttp Interceptor.
 * @see Interceptor
 * @see DataHolder
 */
class SessionInterceptor(private val identifiersHolder: IdentifiersHolder): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader(HerowHeaders.SDK_VERSION_HEADER, BuildConfig.SDK_VERSION)
        val accessToken = identifiersHolder.getAccessToken()
        if (accessToken.isNotEmpty()) {
            requestBuilder.addHeader(HerowHeaders.AUTHORIZATION_HEADER, accessToken)
        }
        val herowId = identifiersHolder.getHerowId()
        if (herowId.isNotEmpty()) {
            requestBuilder.addHeader(HerowHeaders.HEROW_ID_HEADER, herowId)
        }
        val deviceId = identifiersHolder.getDeviceId()
        if (deviceId.isNotEmpty()) {
            requestBuilder.addHeader(HerowHeaders.DEVICE_ID_HEADER, deviceId)
        }
        return chain.proceed(requestBuilder.build())
    }
}