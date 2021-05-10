package io.herow.sdk.connection.cache.model

import java.time.LocalDateTime

data class HerowCapping(
    var campaignId: String = "",
    var razDate: LocalDateTime? = null,
    var count: Int = 0
)