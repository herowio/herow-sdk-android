package io.herow.sdk.detection

import android.app.Application
import androidx.work.Configuration

class DetectionApplication(): Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}