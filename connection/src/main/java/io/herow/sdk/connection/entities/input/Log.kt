package io.herow.sdk.connection.entities.input

import com.squareup.moshi.Json

data class Log(private val data: Map<String, String>,
               @field:Json(name = "d")
               private val date: Long,
               @field:Json(name = "t")
               private val type: String = "app_mobile")