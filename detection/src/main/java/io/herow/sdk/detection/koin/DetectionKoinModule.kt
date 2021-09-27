package io.herow.sdk.detection.koin

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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val dispatcherModule = module {
    factory { provideTestingDispatcherProvider() }
}

val sessionModule = module {
    single { provideDataHolder(get())}
    single { provideSessionHolder(get()) }
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

val databaseModuleTest = module {
    factory { provideHerowDatabaseTest(get()) }
    single { provideZoneDAO(get()) }
    single { providePoiDAO(get()) }
    single { provideCampaignDAO(get()) }
    single { provideZoneRepository(get()) }
    single { providePoiRepository(get()) }
    single { provideCampaignRepository(get()) }
}

private fun provideDataHolder(context: Context): DataHolder = DataHolder(context)

private fun provideTestingDispatcherProvider(): CoroutineDispatcher = Dispatchers.IO

private fun provideSessionHolder(dataHolder: DataHolder): SessionHolder =
    SessionHolder(dataHolder)

private fun provideHerowDatabaseTest(context: Context): HerowDatabase = HerowDatabase.getDatabaseTest(context)

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

