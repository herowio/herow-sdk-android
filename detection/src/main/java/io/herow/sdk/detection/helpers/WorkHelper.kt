package io.herow.sdk.detection.helpers

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import io.herow.sdk.common.helpers.Constants
import io.herow.sdk.connection.HerowPlatform
import io.herow.sdk.connection.SessionHolder
import java.util.concurrent.ExecutionException

object WorkHelper {

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

    fun getWorkOfData(sessionHolder: SessionHolder): HashMap<String, String> {
        val sdkID: String = sessionHolder.getSDKID()
        val sdkKey: String = sessionHolder.getSdkKey()
        val customID: String = sessionHolder.getCustomID()

        return hashMapOf(
            Pair(Constants.SDK_ID, sdkID),
            Pair(Constants.SDK_KEY, sdkKey),
            Pair(Constants.CUSTOM_ID, customID)
        )
    }

    fun getPlatform(sessionHolder: SessionHolder): HashMap<String, HerowPlatform> =
        hashMapOf(Pair(Constants.PLATFORM, sessionHolder.getPlatformName()))
}