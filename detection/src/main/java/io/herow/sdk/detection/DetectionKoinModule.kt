package io.herow.sdk.detection

import android.content.Context
import io.herow.sdk.common.DataHolder
import io.herow.sdk.connection.SessionHolder
import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.dao.PoiDAO
import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.repository.CampaignRepository
import io.herow.sdk.connection.cache.repository.PoiRepository
import io.herow.sdk.connection.cache.repository.ZoneRepository
import io.herow.sdk.connection.database.HerowDatabase
import io.herow.sdk.detection.location.LocationManager
import io.herow.sdk.detection.network.CacheWorker
import io.herow.sdk.detection.network.LogsWorker
import io.herow.sdk.detection.notification.NotificationManager
import io.herow.sdk.detection.zones.ZoneManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val workerModule = module {
    factory { LogsWorker(get(), get(), provideIoDispatcher()) }
    factory { CacheWorker(get(), get(), provideIoDispatcher()) }
    single { LocationManager(get(), provideSessionHolder(get()), provideIoDispatcher()) }
    single { NotificationManager(get(), provideSessionHolder(get()), provideIoDispatcher()) }
    single { ZoneManager(get(), get(), provideIoDispatcher()) }
}

val databaseModule = module {
    single { provideHerowDatabase(get()) }
    single { provideZoneDAO(get()) }
    single { providePoiDAO(get()) }
    single { provideCampaignDAO(get()) }
    single { provideZoneRepository(get()) }
    single { providePoiRepository(get()) }
    single { provideCampaignRepository(get()) }
}

private fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
private fun provideSessionHolder(context: Context): SessionHolder =
    SessionHolder(DataHolder(context))

private fun provideHerowDatabase(context: Context): HerowDatabase =
    HerowDatabase.getDatabase(context)

private fun provideZoneDAO(herowDatabase: HerowDatabase): ZoneDAO = herowDatabase.zoneDAO()
private fun providePoiDAO(herowDatabase: HerowDatabase): PoiDAO = herowDatabase.poiDAO()
private fun provideCampaignDAO(herowDatabase: HerowDatabase): CampaignDAO =
    herowDatabase.campaignDAO()

private fun provideZoneRepository(herowDatabase: HerowDatabase): ZoneRepository =
    ZoneRepository(provideZoneDAO(herowDatabase))

private fun providePoiRepository(herowDatabase: HerowDatabase): PoiRepository = PoiRepository(
    providePoiDAO(herowDatabase)
)

private fun provideCampaignRepository(herowDatabase: HerowDatabase): CampaignRepository =
    CampaignRepository(
        provideCampaignDAO(herowDatabase)
    )

