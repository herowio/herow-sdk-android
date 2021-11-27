package io.herow.sdk.detection.helpers

import io.herow.sdk.connection.cache.model.CacheResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface FakeAPI {
    @GET("universe")
    fun getAnswerToUniverse(): Call<Void>

    @GET("v2/sdk/cache/content/test")
    suspend fun cache(): Response<CacheResult>
}