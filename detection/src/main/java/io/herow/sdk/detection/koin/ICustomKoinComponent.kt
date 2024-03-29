package io.herow.sdk.detection.koin

import io.herow.sdk.detection.HerowInitializer
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

interface ICustomKoinComponent: KoinComponent {

    override fun getKoin(): Koin {
        return if (HerowInitializer.isTesting()) {
            HerowKoinTestContext.koinTest
        } else {
            HerowKoinContext.koin
        }
    }
}