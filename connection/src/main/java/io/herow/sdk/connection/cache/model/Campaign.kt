package io.herow.sdk.connection.cache.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Campaign"
)
data class Campaign(
    @PrimaryKey(autoGenerate = true)
    var campaignID: Long = 0,

    var id: String? = "",
    var name: String? = "",
    var begin: Long? = 0,
    var end: Long? = 0,
    var daysRecurrence: List<String>? = listOf(),
    var startHour: String? = "",
    var stopHour: String? = "",

    @Embedded
    var capping: Capping? = null,
    @Embedded
    var notification: HerowNotification? = null
)
