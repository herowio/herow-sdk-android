package io.herow.sdk.connection

import retrofit2.Call
import retrofit2.http.GET

interface FakeAPI {
    @GET("universe")
    fun getAnswerToUniverse(): Call<Void>
}