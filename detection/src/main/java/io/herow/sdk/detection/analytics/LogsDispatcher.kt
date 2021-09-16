package io.herow.sdk.detection.analytics

import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.logs.Log
import java.util.concurrent.CopyOnWriteArrayList

object LogsDispatcher {

    fun addLogListener(logsListener: ILogsListener) {
        logsListeners.add(logsListener)
    }

    fun unregisterLogListener(logsListener: ILogsListener) {
        logsListeners.remove(logsListener)
    }

    private val logsListeners = CopyOnWriteArrayList<ILogsListener>()

    fun dispatchLogsResult(listOfLogs: List<Log>) {
        GlobalLogger.shared.info(null, "Dispatching logs to: $logsListeners")
        GlobalLogger.shared.info(null, "Dispatching logs: $listOfLogs")
        for (logsListener in logsListeners) {
            if (listOfLogs.isNotEmpty()) {

                logsListener.onLogsToSend(listOfLogs)
            }
        }
    }
}