package io.herow.sdk.connection

data class TokenResult(private val accessToken: String,
                       private val expiresIn: Int)