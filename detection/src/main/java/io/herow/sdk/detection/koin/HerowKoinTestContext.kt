package io.herow.sdk.detection.koin

import android.annotation.SuppressLint
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication

@SuppressLint("StaticFieldLeak")
object HerowKoinTestContext {
    private lateinit var koinTestContext: Context

    val koinTest: Koin by lazy {
        koinApplication {
            androidContext(koinTestContext)
            androidLogger(Level.INFO)
            modules(listOf(databaseModuleTest, dispatcherModule, sessionModule))
        }.koin
    }

    @Synchronized
    fun init(context: Context) {
        koinTestContext = context
    }
}