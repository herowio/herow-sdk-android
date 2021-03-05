package io.herow.sdk.connection.cache.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "Campaign"
)
data class Campaign(
    @PrimaryKey(autoGenerate = true)
    val campaignID: Long = 0,

    var id: String? = "",
    var company: String? = "",
    var createdDate: Long? = 0,
    var modifiedDate: Long? = 0,
    var deleted: Boolean? = false,
    var simpleID: String? = "",
    var name: String? = "",
    var begin: Long? = 0,
    var recurrenceEnabled: Boolean? = false,
    var timeZone: String? = "",

    @Embedded
    var capping: Capping? = null,
    @Embedded
    var trigger: Trigger? = null,
    @Embedded
    var notification: Notification? = null,

    var intervals: List<Interval?> = listOf()
)
