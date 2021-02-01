package io.herow.sdk.connection.logs

import com.google.gson.annotations.SerializedName

data class Log(val data: Map<String, Any>,
               @SerializedName("d")
               val date: Long,
               @SerializedName("t")
               val type: String = "app_mobile")