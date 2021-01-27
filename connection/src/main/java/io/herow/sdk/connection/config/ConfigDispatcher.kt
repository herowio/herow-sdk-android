package io.herow.sdk.connection.config

import java.util.concurrent.CopyOnWriteArrayList

object ConfigDispatcher {
    fun addConfigListener(configListener: ConfigListener) {
        configListeners.add(configListener)
    }
    private val configListeners = CopyOnWriteArrayList<ConfigListener>()

    fun dispatchConfigResult(configResult: ConfigResult) {
        for (configListener in configListeners) {
            configListener.onConfigResult(configResult)
        }
    }
}