package io.herow.sdk.connection.cache.model

import androidx.room.Ignore
import androidx.room.PrimaryKey

data class HerowNotification(
    @PrimaryKey(autoGenerate = true)
    var herowNotificationID: Long = 0,
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
