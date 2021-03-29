package io.herow.sdk.detection.analytics

import io.herow.sdk.connection.logs.Log
import java.util.concurrent.CopyOnWriteArrayList

object LogsDispatcher {
    fun addLogListener(logsListener: LogsListener) {
        logsListeners.add(logsListener)
    }

    private val logsListeners = CopyOnWriteArrayList<LogsListener>()

    fun dispatchLogsResult(listOfLogs: List<Log>) {
        android.util.Log.i("XXX/EVENT", "LogsDispatcher - dispatchLogsResult: $listOfLogs")
        for (logsListener in logsListeners) {
            logsListener.onLogsToSend(listOfLogs)
        }
    }
}