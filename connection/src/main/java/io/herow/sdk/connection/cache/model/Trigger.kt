package io.herow.sdk.connection.cache.model

data class Trigger(
    var onExit: Boolean = false,
    var isPersistent: Boolean = false,
    var dwellTime: Long = 0
)
