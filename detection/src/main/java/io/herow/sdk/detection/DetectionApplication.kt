package io.herow.sdk.detection

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

open class DetectionApplication : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(Log.INFO)
        .build()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DetectionApplication)
            modules(listOf(dispatcherModule, databaseModule))
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }
}