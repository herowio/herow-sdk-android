package io.herow.sdk.detection

import android.app.Application
import androidx.work.Configuration
import io.herow.sdk.common.koin.dispatcherModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinExperimentalAPI
import org.koin.core.context.startKoin

class DetectionApplication : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.INFO)
        .build()

    @KoinExperimentalAPI
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DetectionApplication)
            workManagerFactory()
            modules(listOf(dispatcherModule, workerModule, databaseModule))
        }
    }
}