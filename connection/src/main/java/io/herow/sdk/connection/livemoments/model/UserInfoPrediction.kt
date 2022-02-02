package io.herow.sdk.connection.livemoments.model

import io.herow.sdk.common.data.TagPrediction
import io.herow.sdk.common.data.ZonePrediction

data class UserInfoPrediction(
    val tags: ArrayList<TagPrediction>?,
    val zones: ArrayList<ZonePrediction>?
)
