package io.herow.sdk.connection

import io.herow.sdk.common.logger.GlobalLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitBuilder {
    fun <T> buildRetrofitForAPI(
        sessionHolder: SessionHolder,
        apiURL: String,
        apiClass: Class<T>,
        addLoggingInterceptor: Boolean = false
    ): T {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient(sessionHolder, addLoggingInterceptor))
            .build()

        GlobalLogger.shared.info(null, "Add logging interceptor: $addLoggingInterceptor")

        return retrofit.create(apiClass)
    }

    private fun getOkHttpClient(
        sessionHolder: SessionHolder,
        addLoggingInterceptor: Boolean
    ): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder().addInterceptor(SessionInterceptor(sessionHolder))
        if (addLoggingInterceptor) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
        }

        return okHttpBuilder.build()
    }
}