package io.herow.sdk.connection.cache.model

data class Notification(
    var title: String? = "",
    var description: String? = "",
    var textToSpeech: String? = "",
    var image: String? = "",
    var thumbnail: String? = "",
    var uri: String? = ""
)
