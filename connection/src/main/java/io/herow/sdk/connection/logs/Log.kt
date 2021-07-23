package io.herow.sdk.connection.logs

data class Log(
    val data: Map<String, Any>,
    val t: String = "app_mobile"
)