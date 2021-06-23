package io.herow.sdk.connection.cache.model

import io.herow.sdk.common.helpers.TimeHelper

data class HerowCapping(
    var campaignId: String = "",
    var razDateInTimestamp: Long = TimeHelper.getCurrentTime(),
    var count: Int = 0
)