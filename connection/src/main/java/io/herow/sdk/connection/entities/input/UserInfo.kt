package io.herow.sdk.connection.entities.input

import com.squareup.moshi.Json

data class UserInfo(private val optins: List<Optin>,
                    @field:Json(name = "adId")
                    private val advertiserId: String,
                    @field:Json(name = "adStatus")
                    private val advertiserStatus: Boolean,
                    private val customId: String,
                    @field:Json(name = "lang")
                    private val language: String,
                    @field:Json(name = "offset")
                    private val utcOffsetMs: Int)