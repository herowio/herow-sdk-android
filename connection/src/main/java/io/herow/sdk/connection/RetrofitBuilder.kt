package io.herow.sdk.connection

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitBuilder {
    fun <T> buildRetrofitForAPI(apiURL: String, apiClass: Class<T>): T {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(apiURL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return retrofit.create(apiClass)
    }
}