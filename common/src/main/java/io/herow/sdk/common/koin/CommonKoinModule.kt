package io.herow.sdk.common.koin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val dispatcherModule = module {
    factory { provideIoDispatcher() }
}

private fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO