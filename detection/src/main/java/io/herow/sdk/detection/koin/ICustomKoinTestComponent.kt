package io.herow.sdk.detection.koin

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

interface ICustomKoinTestComponent: KoinComponent {
    override fun getKoin(): Koin = HerowKoinTestContext.koinTest
}