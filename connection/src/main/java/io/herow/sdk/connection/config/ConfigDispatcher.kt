package io.herow.sdk.connection.config

import io.herow.sdk.common.logger.GlobalLogger
import java.util.concurrent.CopyOnWriteArrayList

object ConfigDispatcher {

    fun addConfigListener(configListener: IConfigListener) {
        configListeners.add(configListener)
    }

    private val configListeners = CopyOnWriteArrayList<IConfigListener>()

    fun dispatchConfigResult(configResult: ConfigResult) {
        for (configListener in configListeners) {
            GlobalLogger.shared.info(
                null,
                "Dispatching config to: $configListener"
            )
            configListener.onConfigResult(configResult)
        }
    }
}