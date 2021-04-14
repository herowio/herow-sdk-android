package io.herow.sdk.detection.analytics.model

import io.herow.sdk.common.helpers.DeviceHelper
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.BuildConfig
import io.herow.sdk.detection.analytics.ApplicationData

open class HerowLogData : HashMap<String, Any>() {
    companion object {
        const val SUBTYPE = "subtype"
        const val APP_STATE = "app_state"
        const val DATE = "date"
        const val UA = "ua"
        const val LIB_VERSION = "lib_version"
        const val APPLICATION_NAME = "application_name"
        const val APPLICATION_VERSION = "application_version"
        const val PHONE_ID = "phone_id"
        const val HEROW_ID = "herow_id"
    }

    fun enrich(
        applicationData: ApplicationData,
        sessionHolder: SessionHolder
    ) {
        this[UA] = DeviceHelper.getUserAgent()
        this[LIB_VERSION] = BuildConfig.SDK_VERSION
        this[DATE] = TimeHelper.getCurrentTime()
        this[APPLICATION_NAME] = applicationData.applicationName
        this[APPLICATION_VERSION] = applicationData.applicationVersion
        this[PHONE_ID] = sessionHolder.getDeviceId()
        this[HEROW_ID] = sessionHolder.getHerowId()
    }
}