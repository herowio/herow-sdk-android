package io.herow.sdk.connection.cache.repository

import io.herow.sdk.connection.cache.dao.HerowNotificationDAO
import io.herow.sdk.connection.cache.model.HerowNotification

class HerowNotificationRepository(
    private val herowNotificationDAO: HerowNotificationDAO
): IHerowNotificationRepository {

    override fun insert(herowNotification: HerowNotification) = herowNotificationDAO.insert(herowNotification)
    override fun getFiftyFirstNotifications(): List<HerowNotification>? = herowNotificationDAO.getFiftyFirstNotifications()
}