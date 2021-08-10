package io.herow.sdk.connection.cache.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(
    tableName = "HerowNotification"
)
data class HerowNotification(
    @PrimaryKey(autoGenerate = true)
    var herowNotificationID: Long = 0,
    var owner: String = "",
    var title: String? = "",
    var description: String? = "",
    @Ignore
    var textToSpeech: String? = "",
    @Ignore
    var image: String? = "",
    @Ignore
    var thumbnail: String? = "",
    @Ignore
    var uri: String? = ""
)
