package io.herow.sdk.connection

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitBuilder {
    fun <T> buildRetrofitForAPI(apiURL: String, apiClass: Class<T>): T {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return retrofit.create(apiClass)
    }

    fun <T> buildRetrofitForUnitTests(apiURL: String, apiClass: Class<T>): T {
        val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(apiClass)
    }
}