package io.herow.sdk.connection

import android.content.Context
import io.herow.sdk.common.DataHolder
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Allow us to add necessary headers to request as soon as we have saved them in SharedPreferences
 * using an OkHttp Interceptor.
 * @see Interceptor
 * @see DataHolder
 */
class SessionInterceptor(context: Context): Interceptor {
    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val HEROW_ID_HEADER = "X-HEROW-ID"
        const val DEVICE_ID_HEADER = "X-DEVICE-ID"

        private const val KEY_ACCESS_TOKEN = "connection.access_token"
        private const val KEY_DEVICE_ID = "connection.device_id"
        private const val KEY_HEROW_ID = "connection.herow_id"
    }
    private val dataHolder = DataHolder(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val accessToken = dataHolder.get<String>(KEY_ACCESS_TOKEN)
        if (accessToken.isNotEmpty()) {
            requestBuilder.addHeader(AUTHORIZATION_HEADER, accessToken)
        }
        val herowId = dataHolder.get<String>(KEY_HEROW_ID)
        if (herowId.isNotEmpty()) {
            requestBuilder.addHeader(HEROW_ID_HEADER, herowId)
        }
        val deviceId = dataHolder.get<String>(KEY_DEVICE_ID)
        if (deviceId.isNotEmpty()) {
            requestBuilder.addHeader(DEVICE_ID_HEADER, deviceId)
        }
        return chain.proceed(requestBuilder.build())
    }
}