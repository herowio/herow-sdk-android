package io.herow.sdk.connection

import io.herow.sdk.connection.cache.CacheResult
import io.herow.sdk.connection.config.ConfigResult
import io.herow.sdk.connection.token.TokenResult
import io.herow.sdk.connection.userinfo.UserInfoResult
import retrofit2.Response
import retrofit2.http.*

interface HerowAPI {
    companion object {
        const val PRE_PROD_BASE_URL = "https://m-preprod.herow.io"
        const val PROD_BASE_URL = "https://m.herow.io"
    }

    @Headers(
        "Cache-Control: noCache",
    )
    @FormUrlEncoded
    @POST("auth/authorize/token")
    suspend fun token(@Field("username") username: String,
                      @Field("password") password: String,
                      @Field("client_id") clientId: String,
                      @Field("client_secret") clientSecret: String,
                      @Field("redirect_uri") redirectUri: String,
                      @Field("grant_type") grantType: String = "password"): Response<TokenResult>

    @Headers(
        "Content-Type: application/json",
    )
    @PUT("v2/sdk/userinfo")
    suspend fun userInfo(@Body body: String): Response<UserInfoResult>

    @Headers(
        "Content-Type: application/json",
    )
    @GET("v2/sdk/config")
    suspend fun config(): Response<ConfigResult>

    @Headers(
        "Content-Type: application/json",
    )
    @GET("v2/sdk/cache/content/{geohash}")
    suspend fun cache(@Path("geohash") geohash: String): Response<CacheResult>

    @Headers(
        "Content-Type: application/json",
    )
    @POST("stat/queue/multi")
    suspend fun logs(@Body body: String): Response<Void>
}