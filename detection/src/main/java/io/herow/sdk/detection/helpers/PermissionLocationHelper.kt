package io.herow.sdk.detection.helpers

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.herow.sdk.connection.userinfo.PermissionLocation
import io.herow.sdk.connection.userinfo.Precision
import io.herow.sdk.connection.userinfo.Status

object PermissionLocationHelper {

    fun treatActualPermissions(context: Context): PermissionLocation {
        val actualPermissions = getActualPermission(context)
        val permissionMapToReturn = PermissionLocation(Precision.FINE, Status.DENIED)

        var fineStatus = ""
        var backgroundStatus = ""
        var allDenied = true

        for (value in actualPermissions.values) {
            if (value != Status.DENIED.name) {
                allDenied = false
                break
            }
        }

        if (allDenied) {
            return permissionMapToReturn.apply {
                precision = Precision.COARSE
                status = Status.DENIED
            }
        }

        for (key in actualPermissions.keys) {
            if (key == Precision.FINE.name) {
                fineStatus = actualPermissions[key].toString()
            }

            if (key == Precision.BACKGROUND.name) {
                backgroundStatus = actualPermissions[key].toString()
            }
        }

        return permissionMapToReturn.apply {
            status = if (backgroundStatus.isNotEmpty() && backgroundStatus == Status.GRANTED.name) {
                Status.ALWAYS
            } else {
                Status.WHILE_IN_USE
            }
            precision = if (fineStatus.isNotEmpty() && fineStatus == Status.GRANTED.name) {
                Precision.FINE
            } else {
                Precision.COARSE
            }
        }
    }

    private fun getActualPermission(context: Context): HashMap<String, String> {
        val permissions = HashMap<String, String>()

        permissions[Precision.FINE.name] = if (ContextCompat.checkSelfPermission(
                context,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Status.DENIED.name
        } else {
            Status.GRANTED.name
        }

        permissions[Precision.COARSE.name] = if (ContextCompat.checkSelfPermission(
                context,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Status.DENIED.name
        } else {
            Status.GRANTED.name
        }

        permissions[Precision.BACKGROUND.name] = if (ContextCompat.checkSelfPermission(
                context,
                ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Status.DENIED.name
        } else {
            Status.GRANTED.name
        }

        return permissions
    }
}
