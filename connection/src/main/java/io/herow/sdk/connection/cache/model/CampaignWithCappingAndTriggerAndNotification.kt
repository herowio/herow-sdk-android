package io.herow.sdk.connection.cache.model

import androidx.room.Embedded
import androidx.room.Relation

data class CampaignWithCappingAndTriggerAndNotification(
    @Embedded val campaign: Campaign,
    @Relation(
        parentColumn = "capping",
        entityColumn = "cappingCampaignID"
    )
    val capping: Capping,

    @Relation(
        parentColumn = "trigger",
        entityColumn = "triggerCampaignID"
    )
    val trigger: Trigger,

    @Relation(
        parentColumn = "notification",
        entityColumn = "notificationCampaignID"
    )
    val notification: Notification
)