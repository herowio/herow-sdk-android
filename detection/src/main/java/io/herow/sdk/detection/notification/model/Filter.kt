package io.herow.sdk.detection.notification.model

import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.detection.notification.IFilter

class Filter: IFilter {

    //TODO Update when you get enough informations about it
    override fun createNotificationForCampagn(campaign: Campaign): Boolean {
        return true
    }
}