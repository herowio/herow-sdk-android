package io.herow.sdk.connection.cache.model.mapper

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.HerowCapping

data class HerowCappingMapper(
    var campaignId: String = "",
    var razTimestamp: Long = 0L,
    var count: Int = 0
) {

    fun convertCappingToMapper(herowCapping: HerowCapping): HerowCappingMapper {
        return HerowCappingMapper(
            campaignId = herowCapping.campaignId,
            razTimestamp = TimeHelper.convertDateToMilliSeconds(herowCapping.razDate),
            count = herowCapping.count
        )
    }
}
