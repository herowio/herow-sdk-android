package io.herow.sdk.detection.helpers

import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.connection.SessionHolder

/**
 * Token and UserInfo workflow
 */
object TokenUserInfoHelper {

    /**
     * Check if token is usable
     */
    fun isTokenUsable(sessionHolder: SessionHolder): Boolean {
        if (sessionHolder.getAccessToken()
                .isEmpty() || isTokenExpired(sessionHolder.getTimeOutToken())
        ) {
            return false
        }

        return true
    }

    /**
     * Check if token time is still valid
     */
    private fun isTokenExpired(timeoutTime: Long): Boolean {
        if (timeoutTime < TimeHelper.getCurrentTime()) {
            return false
        }

        return true
    }
}