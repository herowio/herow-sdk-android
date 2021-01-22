package io.herow.sdk.detection.helpers

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException

object WorkHelper {
    fun isWorkScheduled(workManager: WorkManager, tag: String): Boolean {
        val workersList: ListenableFuture<List<WorkInfo>> = workManager.getWorkInfosByTag(tag)
        return try {
            val workInfoList: List<WorkInfo> = workersList.get()
            for (workInfo in workInfoList) {
                val state = workInfo.state
                if (state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED) {
                    return true
                }
            }
            return false
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }

    fun isWorkNotScheduled(workManager: WorkManager, tag: String): Boolean {
        return !isWorkScheduled(workManager, tag)
    }
}