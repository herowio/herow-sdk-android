package io.herow.sdk.common.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import android.provider.Settings

object DeviceHelper {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getUserAgent(): String {
        return System.getProperty("http.agent") ?: "unknown user-agent"
    }

    fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager

        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}