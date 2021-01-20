package io.herow.sdk.connection

import io.herow.sdk.common.IdentifiersHolder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitBuilder {
    fun <T> buildRetrofitForAPI(identifiersHolder: IdentifiersHolder,
                                apiURL: String,
                                apiClass: Class<T>,
                                addLoggingInterceptor: Boolean = false): T {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient(identifiersHolder, addLoggingInterceptor))
            .build()
        return retrofit.create(apiClass)
    }

    private fun getOkHttpClient(identifiersHolder: IdentifiersHolder, addLoggingInterceptor: Boolean): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder().addInterceptor(SessionInterceptor(identifiersHolder))
        if (addLoggingInterceptor) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpBuilder.build()
    }
}