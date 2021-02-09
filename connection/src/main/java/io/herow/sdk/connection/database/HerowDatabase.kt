package io.herow.sdk.connection.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.herow.sdk.connection.cache.dao.CampaignDAO
import io.herow.sdk.connection.cache.dao.PoiDAO
import io.herow.sdk.connection.cache.dao.ZoneDAO
import io.herow.sdk.connection.cache.model.*

@Database(
    entities = [Access::class, Campaign::class, Capping::class, Interval::class,
        Notification::class, Poi::class, Trigger::class, Zone::class],
    version = 1,
    exportSchema = false
)
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