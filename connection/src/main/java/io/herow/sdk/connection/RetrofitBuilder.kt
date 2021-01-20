package io.herow.sdk.connection

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitBuilder {
    fun <T> buildRetrofitForAPI(context: Context,
                                apiURL: String,
                                apiClass: Class<T>,
                                addLoggingInterceptor: Boolean = false): T {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(getOkHttpClient(context, addLoggingInterceptor))
            .build()
        return retrofit.create(apiClass)
    }

    private fun getOkHttpClient(context: Context, addLoggingInterceptor: Boolean): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder().addInterceptor(SessionInterceptor(context))
        if (addLoggingInterceptor) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpBuilder.build()
    }
}