package io.herow.sdk.common.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings

object DeviceHelper {
    var testing: Boolean = false

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return if (testing) {
            "androidIDForTest123"
        } else {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }

    fun getUserAgent(): String {
        return System.getProperty("http.agent") ?: "unknown user-agent"
    }

    fun getBatteryLevel(context: Context): Int {
        val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun getCurrentAndroidVersion(context: Context): Int {
        return Build.VERSION.SDK_INT
    }
}