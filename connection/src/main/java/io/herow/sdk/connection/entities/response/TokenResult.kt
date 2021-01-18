package io.herow.sdk.connection.entities.response

data class TokenResult(private val accessToken: String,
                       private val expiresIn: Int) {
    fun getToken(): String {
        return "OAuth $accessToken"
    }
}