package com.corapana.filetransfer

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionManager {

    private var callback: ((Boolean) -> Unit)? = null

    fun checkAndRequest(
        context: Context,
        permissionRequester: PermissionRequester,
        permissions: Array<String>,
        rationaleMessage: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        // Save callback
        callback = onResult

        // Check if all permissions are already granted
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            onResult(true)
            return
        }

        PermissionHelper.showPermissionDialog(context, rationaleMessage)
            .setPositiveButton("OK") { _, _ ->
                requestPermissions(permissionRequester, permissions)
            }
            .setNegativeButton("Cancel") { _, _ ->
                onResult(false)
            }
            .show()

    }

    private fun requestPermissions(permissionRequester: PermissionRequester, permissions: Array<String>) {
        permissionRequester.requestPermissions(permissions, callback)
    }
}