package io.herow.sdk.detection.network.model

import java.net.URL

data class RetrofitConnectionObject(
    val username: String,
    val password: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: URL,
    val grantType: String = "password"
)