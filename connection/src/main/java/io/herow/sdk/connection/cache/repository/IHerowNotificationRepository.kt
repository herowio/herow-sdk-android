package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.model.HerowNotification

interface IHerowNotificationRepository {
    fun insert(herowNotification: HerowNotification)
    fun getFiftyFirstNotifications(): List<HerowNotification>?
}