package io.herow.sdk.connection.logs

import com.google.gson.annotations.SerializedName

data class Log(private val data: Map<String, Any>,
               @SerializedName("d")
               private val date: Long,
               @SerializedName("t")
               private val type: String = "app_mobile")