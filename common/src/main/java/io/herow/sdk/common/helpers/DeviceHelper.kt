package io.herow.sdk.common.helpers

import android.content.Context
import android.provider.Settings
import java.util.*

object DeviceHelper {
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
    }

    fun getUserAgent(): String {
        return System.getProperty("http.agent") ?: "unknown user-agent"
    }

    fun getDefaultLanguage(): String {
        return try {
            Locale.getDefault().isO3Language
        } catch (exception: MissingResourceException) {
            "eng"
        }
    }
}