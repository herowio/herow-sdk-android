package io.herow.sdk.connection.database

import android.app.Application

class ConnectionMainApplication: Application() {

    companion object {
        var herowDatabase: HerowDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()

        herowDatabase = HerowDatabase.getDatabase(applicationContext)
    }
}