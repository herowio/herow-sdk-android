package io.herow.sdk.connection.token

import io.herow.sdk.connection.BuildConfig
import io.herow.sdk.connection.HerowPlatform

data class PlatformData(private val platform: HerowPlatform) {
    val clientId: String
    val clientSecret: String
    val redirectUri: String

    init {
        if (platform == HerowPlatform.PRE_PROD) {
            clientId = BuildConfig.PRE_PROD_CLIENT_ID
            clientSecret = BuildConfig.PRE_PROD_CLIENT_SECRET
            redirectUri = BuildConfig.PRE_PROD_REDIRECT_URI
        } else {
            clientId = BuildConfig.PROD_CLIENT_ID
            clientSecret = BuildConfig.PROD_CLIENT_SECRET
            redirectUri = BuildConfig.PROD_REDIRECT_URI
        }
    }
}