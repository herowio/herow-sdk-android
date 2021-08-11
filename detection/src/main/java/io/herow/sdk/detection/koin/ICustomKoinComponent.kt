package io.herow.sdk.detection.koin

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

interface ICustomKoinComponent: KoinComponent {
    override fun getKoin(): Koin = HerowKoinContext.koin
}