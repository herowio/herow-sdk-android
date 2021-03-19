package io.herow.sdk.detection.clickandcollect

import java.util.concurrent.CopyOnWriteArrayList

object ClickAndCollectDispatcher {
    fun registerClickAndCollectListener(listener: ClickAndCollectListener) {
        clickAndCollectListener.add(listener)
    }

    fun unregisterClickAndCollectListener(listener: ClickAndCollectListener) {
        clickAndCollectListener.remove(listener)
    }

    private val clickAndCollectListener = CopyOnWriteArrayList<ClickAndCollectListener>()

    fun didStartClickAndCollect() {
        for (listener in clickAndCollectListener) {
            listener.didStartClickAndConnect()
        }
    }

    fun didStopClickAndCollect() {
        for (listener in clickAndCollectListener) {
            listener.didStopClickAndConnect()
        }
    }
}