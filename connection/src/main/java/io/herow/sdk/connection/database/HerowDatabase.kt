package io.herow.sdk.connection.database

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

@Database(
    entities = [Campaign::class, Poi::class, Zone::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HerowDatabase : RoomDatabase() {

    abstract fun campaignDAO(): CampaignDAO
    abstract fun zoneDAO(): ZoneDAO
    abstract fun poiDAO(): PoiDAO

    companion object {
        @Volatile
        private var INSTANCE: HerowDatabase? = null

        fun getDatabase(context: Context): HerowDatabase {
            INSTANCE = INSTANCE ?: Room.databaseBuilder(
                context,
                HerowDatabase::class.java,
                "herow_BDD"
            ).build()

            return INSTANCE as HerowDatabase
        }
    }
}