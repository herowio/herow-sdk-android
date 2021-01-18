package io.herow.sdk.connection

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface HerowAPI {
    companion object {
        const val BASE_URL = "https://m-preprod.herow.io"
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
}