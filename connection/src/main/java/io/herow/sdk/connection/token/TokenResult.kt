package io.herow.sdk.connection.token

data class TokenResult(private val accessToken: String,
                       private val expiresIn: Int) {
    fun getToken(): String {
        return "OAuth $accessToken"
    }
}