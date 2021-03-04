package io.herow.sdk.connection.userinfo

import com.google.gson.annotations.SerializedName

data class UserInfo(
    private val optins: ArrayList<Optin>,
    @SerializedName("adId")
    private val advertiserId: String?,
    private val customId: String
)
