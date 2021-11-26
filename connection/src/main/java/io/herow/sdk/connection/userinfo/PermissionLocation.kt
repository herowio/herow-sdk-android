package io.herow.sdk.connection.userinfo

import com.google.gson.annotations.SerializedName

data class PermissionLocation(
    @SerializedName("precision")
    var precision: Precision,
    @SerializedName("status")
    var status: Status
)