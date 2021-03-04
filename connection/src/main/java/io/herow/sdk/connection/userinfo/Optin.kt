package io.herow.sdk.connection.userinfo

data class Optin(private val type: String = "USER_DATA",
                 val value: Boolean)