package io.herow.sdk.connection.logs

import com.squareup.moshi.Json

data class Log(private val data: Map<String, Any>,
               @field:Json(name = "d")
               private val date: Long,
               @field:Json(name = "t")
               private val type: String = "app_mobile")