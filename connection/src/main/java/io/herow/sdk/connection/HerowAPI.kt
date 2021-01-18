package io.herow.sdk.connection

import io.herow.sdk.connection.entities.response.CacheResult
import io.herow.sdk.connection.entities.response.ConfigResult
import io.herow.sdk.connection.entities.response.TokenResult
import io.herow.sdk.connection.entities.response.UserInfoResult
import retrofit2.Response
import retrofit2.http.*

interface HerowAPI {
    companion object {
        const val PRE_PROD_BASE_URL = "https://m-preprod.herow.io"
        const val PROD_BASE_URL = "https://m.herow.io"
    }

    @FormUrlEncoded
    @Headers(
        "Cache-Control: noCache",
        "X-VERSION: 7.0.0"
    )
    @POST("auth/authorize/token")
    suspend fun token(@Field("username") username: String,
                      @Field("password") password: String,
                      @Field("client_id") clientId: String,
                      @Field("client_secret") clientSecret: String,
                      @Field("redirect_uri") redirectUri: String,
                      @Field("grant_type") grantType: String = "password"): Response<TokenResult>

    @Headers(
        "Content-Type: application/json",
        "X-VERSION: 7.0.0"
    )
    @PUT("v2/sdk/userinfo")
    suspend fun userInfo(@Header("Authorization") token: String,
                         @Header("X-DEVICE-ID") deviceId: String,
                         @Body body: String): Response<UserInfoResult>

    @Headers(
        "Content-Type: application/json",
        "X-VERSION: 7.0.0"
    )
    @GET("v2/sdk/config")
    suspend fun config(@Header("Authorization") token: String,
                       @Header("X-DEVICE-ID") deviceId: String,
                       @Header("X-HEROW-ID") herowId: String): Response<ConfigResult>

    @Headers(
        "Content-Type: application/json",
        "X-VERSION: 7.0.0"
    )
    @GET("v2/sdk/cache/content/{geohash}")
    suspend fun cache(@Header("Authorization") token: String,
                      @Header("X-DEVICE-ID") deviceId: String,
                      @Header("X-HEROW-ID") herowId: String,
                      @Path("geohash") geohash: String): Response<CacheResult>

    @Headers(
        "Content-Type: application/json",
        "X-VERSION: 7.0.0"
    )
    @POST("stat/queue/multi")
    suspend fun logs(@Header("Authorization") token: String,
                     @Header("X-DEVICE-ID") deviceId: String,
                     @Header("X-HEROW-ID") herowId: String,
                     @Body body: String): Response<Void>
}