package io.herow.sdk.connection.cache.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Trigger"
)
data class Trigger(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val triggerCampaignID: Int,
    val onExit: Boolean,
    val isPersistent: Boolean,
    val dwellTime: Long
)
