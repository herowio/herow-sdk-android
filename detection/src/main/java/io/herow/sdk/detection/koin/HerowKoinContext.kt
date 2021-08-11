package io.herow.sdk.detection.koin

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication

object HerowKoinContext {
    private lateinit var appContext: Context

    val koin: Koin by lazy {
        koinApplication {
            androidContext(appContext)
            androidLogger(Level.INFO)
            modules(listOf(dispatcherModule, databaseModule))
        }.koin
    }

    @Synchronized
    fun init(context: Context) {
        check(!::appContext.isInitialized) { "Already initialized!" }

        appContext = context.applicationContext
    }
}



