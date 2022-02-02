package io.herow.sdk.connection.userinfo

import com.google.gson.annotations.SerializedName
import io.herow.sdk.connection.livemoments.model.UserInfoPrediction

data class UserInfo(
    private val optins: ArrayList<Optin>,
    @SerializedName("adId")
    private val advertiserId: String?,
    private val customId: String,
    @SerializedName("location")
    private val permissionLocation: PermissionLocation,
    private val predictions: UserInfoPrediction?
)

