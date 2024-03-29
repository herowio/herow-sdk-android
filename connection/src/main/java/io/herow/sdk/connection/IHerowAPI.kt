package io.herow.sdk.connection

import androidx.annotation.Keep
import io.herow.sdk.connection.cache.model.CacheResult
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.UserInfoResult
import retrofit2.Response
import retrofit2.http.*

@Keep
interface IHerowAPI {

    companion object {
        const val TEST_BASE_URL = "https://herow-sdk-backend-poc.ew.r.appspot.com"
    }

    @Headers(
        "Cache-Control: noCache",
        "Content-Type: application/json"
    )
    @POST("auth/authorize/token")
    suspend fun token(@Body body: String): Response<TokenResult>

    @Headers(
        "Content-Type: application/json",
    )
    @PUT("v2/sdk/userinfo")
    suspend fun userInfo(@Body body: String): Response<UserInfoResult>

    @Headers(
        "Content-Type: application/json"
    )
    @GET("v2/sdk/config")
    suspend fun config(): Response<ConfigResult>

    @Headers(
        "Content-Type: application/json",
    )
    @GET("v2/sdk/cache/content/{geohash}")
    suspend fun cache(@Path("geohash") geohash: String): Response<CacheResult>

    @Headers(
        "Content-Type: application/json"
    )
    @POST("stat/queue")
    suspend fun log(@Body body: String): Response<Void>
}