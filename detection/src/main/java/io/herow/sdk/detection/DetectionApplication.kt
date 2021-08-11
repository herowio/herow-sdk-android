package io.herow.sdk.detection

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import org.koin.core.context.stopKoin

open class DetectionApplication : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(Log.INFO)
        .build()

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }
}