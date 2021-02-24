package io.herow.sdk.connection.token

import io.herow.sdk.common.helpers.TimeHelper

data class TokenResult(
    private val accessToken: String,
    private val expiresIn: Int
) {
    fun getToken(): String {
        return "OAuth $accessToken"
    }

    /**
     * Return time before token needs to be refreshed
     * Includes expiring delay and margin of 30 seconds
     */
    fun getTimeoutTime(time: Long): Long {
        return time + expiresIn - TimeHelper.THIRTY_SECONDS_MS
    }
}