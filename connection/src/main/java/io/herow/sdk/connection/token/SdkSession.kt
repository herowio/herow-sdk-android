package io.herow.sdk.connection.token

data class SdkSession(
    val sdkId: String,
    val sdkKey: String
) {
    fun hasBeenFilled(): Boolean {
        return sdkId.isNotEmpty() && sdkKey.isNotEmpty()
    }
}