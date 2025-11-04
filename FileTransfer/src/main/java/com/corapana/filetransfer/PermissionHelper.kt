package com.corapana.filetransfer

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AlertDialog

object PermissionHelper {

    fun showPermissionDialog(
        context: Context,
        message: String? = null
    ): AlertDialog.Builder {
        val finalMessage = if (message.isNullOrEmpty()) {
            "Please allow permissions to continue."
        } else {
            message
        }

        return AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage(finalMessage)
    }


    fun getStoragePermissions(): Array<String> {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        return permissions
    }

    fun getCameraPermission(): Array<String> {
        return arrayOf(Manifest.permission.CAMERA)
    }

}
