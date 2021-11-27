package io.herow.sdk.detection.analytics

import io.herow.sdk.connection.logs.Log

interface ILogsListener {
    fun onLogsToSend(listOfLogs: List<Log>)
}