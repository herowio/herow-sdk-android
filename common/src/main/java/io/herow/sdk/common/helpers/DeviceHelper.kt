package io.herow.sdk.common.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

object DeviceHelper {
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getUserAgent(): String {
        return System.getProperty("http.agent") ?: "unknown user-agent"
    }
}