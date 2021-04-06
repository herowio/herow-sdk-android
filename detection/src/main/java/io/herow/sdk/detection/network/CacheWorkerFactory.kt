package io.herow.sdk.detection.network

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class CacheWorkerFactory :
    WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            CacheWorker::class.java.name -> {
                CacheWorker(appContext, workerParameters)
            }
            else -> {
                null
            }
        }
    }
}