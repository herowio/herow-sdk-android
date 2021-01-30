package io.herow.sdk.connection.userinfo

import com.google.gson.annotations.SerializedName

// TODO: add location_status
data class UserInfo(private val optins: ArrayList<Optin>,
                    @SerializedName("adId")
                    private val advertiserId: String?,
                    private val customId: String,
                    @SerializedName("offset")
                    private val utcOffsetMs: Int)