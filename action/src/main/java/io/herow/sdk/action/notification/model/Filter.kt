package io.herow.sdk.action.notification.model

import io.herow.sdk.action.notification.IFilter
import io.herow.sdk.connection.cache.model.Campaign

class Filter: IFilter {

    //TODO Update when you get enough informations about it
    override fun createNotificationForCampagn(campaign: Campaign): Boolean {
        return true
    }
}