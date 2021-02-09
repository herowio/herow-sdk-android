package io.herow.sdk.connection.cache.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "Campaign",
    foreignKeys = [ForeignKey(
        entity = Zone::class,
        parentColumns = arrayOf("listOfCampaigns"),
        childColumns = arrayOf("campaignID"),
        onDelete = ForeignKey.NO_ACTION
    )],
)
data class Campaign(

    @PrimaryKey(autoGenerate = false)
    val campaignID: String,
    val company: String,
    val createdDate: Long,
    val modifiedDate: Long,
    val deleted: Boolean,
    val simpleID: String,
    val name: String,
    val begin: Long,

    val intervals: List<Interval>,

    val recurrenceEnabled: Boolean,
    val timeZone: String,
    val capping: Capping,
    val trigger: Trigger,
    val notification: Notification
)
