package io.herow.sdk.connection.cache.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Interval",
    foreignKeys = [ForeignKey(
        entity = Campaign::class,
        parentColumns = arrayOf("intervals"),
        childColumns = arrayOf("intervalID"),
        onDelete = ForeignKey.NO_ACTION
    )]
)
data class Interval(
    @PrimaryKey(autoGenerate = true)
    val intervalID: Int,
    val start: Long,
    val end: Long
)
