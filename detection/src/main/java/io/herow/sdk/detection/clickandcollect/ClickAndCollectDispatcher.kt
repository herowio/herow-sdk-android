package io.herow.sdk.detection.clickandcollect

import java.util.concurrent.CopyOnWriteArrayList

object ClickAndCollectDispatcher {

    fun registerClickAndCollectListener(listener: IClickAndCollectListener) {
        clickAndCollectListener.add(listener)
    }

    fun unregisterClickAndCollectListener(listener: IClickAndCollectListener) {
        clickAndCollectListener.remove(listener)
    }

    private val clickAndCollectListener = CopyOnWriteArrayList<IClickAndCollectListener>()

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