package io.herow.sdk.connection.cache.model

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.cache.model.mapper.HerowCappingMapper
import java.time.LocalDateTime

data class HerowCapping(
    var campaignId: String = "",
    var razDate: LocalDateTime = TimeHelper.getCurrentLocalDateTime(),
    var count: Int = 0
) {
    
    fun convertMapperToCapping(herowCappingMapper: HerowCappingMapper): HerowCapping {
        return HerowCapping(
            campaignId = herowCappingMapper.campaignId,
            razDate = TimeHelper.convertMilliSecondsToDate(herowCappingMapper.razTimestamp),
            count = herowCappingMapper.count
        )
        
    }
}


