package io.herow.sdk.connection

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitBuilder {
    fun <T> buildRetrofitForAPI(context: Context, apiURL: String, apiClass: Class<T>): T {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient(context))
            .build()
        return retrofit.create(apiClass)
    }

    private fun getOkHttpClient(context: Context): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(SessionInterceptor(context))
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }
}