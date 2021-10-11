package io.herow.sdk.detection.helpers

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.detection.koin.ICustomKoinComponent
import org.koin.core.component.inject
import java.util.concurrent.ExecutionException

object WorkHelper : ICustomKoinComponent {
    private val sessionHolder: SessionHolder by inject()

    private fun isWorkScheduled(workManager: WorkManager, tag: String): Boolean {
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

    fun isWorkNotScheduled(workManager: WorkManager, tag: String): Boolean = !isWorkScheduled(workManager, tag)
}