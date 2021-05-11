package io.herow.sdk.detection.analytics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class ApplicationData(context: Context) {
    companion object {
        const val UNKNOWN = "unknown"
    }
    var applicationName: String
    private var applicationPackage: String
    var applicationVersion: String
    private var applicationCode: Long

    init {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            applicationName = context.getString(packageInfo.applicationInfo.labelRes)
            applicationPackage = context.packageName
            applicationVersion = packageInfo.versionName
            applicationCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            applicationName = UNKNOWN
            applicationPackage = UNKNOWN
            applicationVersion = UNKNOWN
            applicationCode = -1
        }
    }
}