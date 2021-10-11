package io.herow.sdk.detection.session

import io.herow.sdk.common.logger.GlobalLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.URL

object RetrofitBuilder {

    fun <T> buildRetrofitForAPI(
        apiURL: String,
        apiClass: Class<T>,
        addLoggingInterceptor: Boolean = false
    ): T {

        val url = URL(apiURL)
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient(addLoggingInterceptor))
            .build()

        GlobalLogger.shared.info(null, "Add logging interceptor: $addLoggingInterceptor")

        return retrofit.create(apiClass)
    }

    private fun getOkHttpClient(
        addLoggingInterceptor: Boolean
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder().addInterceptor(
            SessionInterceptor()
        )
        if (addLoggingInterceptor) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
        }

        return okHttpBuilder.build()
    }
}