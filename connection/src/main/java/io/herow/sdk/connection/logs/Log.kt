package io.herow.sdk.connection.logs

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Log(val data: Map<String, Any>,
               @SerializedName("t")
               val type: String = "app_mobile")