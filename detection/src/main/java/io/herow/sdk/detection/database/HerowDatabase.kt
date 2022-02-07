package io.herow.sdk.detection.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.dao.PoiDAO
import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.model.Campaign
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.cache.model.Zone
import io.herow.sdk.connection.cache.utils.Converters
import io.herow.sdk.detection.livemoment.model.dao.IPeriodDAO

@Database(
    entities = [Campaign::class, Poi::class, Zone::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HerowDatabase : RoomDatabase() {

    abstract fun campaignDAO(): CampaignDAO
    abstract fun zoneDAO(): ZoneDAO
    abstract fun poiDAO(): PoiDAO
    abstract fun periodDAO(): IPeriodDAO

    companion object {
        @Volatile
        private var INSTANCE: HerowDatabase? = null

        fun getDatabase(context: Context): HerowDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context,
                HerowDatabase::class.java,
                "herow_BDD"
            ).fallbackToDestructiveMigration()
                .build()

            INSTANCE = instance
            instance
        }

        fun getDatabaseTest(context: Context): HerowDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.inMemoryDatabaseBuilder(
                context,
                HerowDatabase::class.java
            )
                .fallbackToDestructiveMigration()
                .build()

            INSTANCE = instance
            instance
        }
    }
}