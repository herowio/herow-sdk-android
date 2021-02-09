package io.herow.sdk.connection.database

import android.app.Application

class MainApplication : Application() {

    companion object {
        var herowDatabase: HerowDatabase? = null

        fun getDatabase(): HerowDatabase? {
            return herowDatabase
        }
    }

    override fun onCreate() {
        super.onCreate()

        herowDatabase = HerowDatabase.getDatabase(applicationContext)
    }
}