package io.herow.sdk.connection.cache.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Capping"
)
data class Capping(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var cappingCampaignID: Int,
    val maxNumberNotifications: Int,
    val minTimeBetweenTwoNotifications: Long
)
