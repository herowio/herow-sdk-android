package io.herow.sdk.connection.cache.model

data class Capping(
    var maxNumberNotifications: Int = 0,
    var minTimeBetweenTwoNotifications: Long = 0
)
