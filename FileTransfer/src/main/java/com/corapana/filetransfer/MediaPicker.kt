package com.corapana.filetransfer

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore


class MediaPicker(private val resultManager: ResultManager) {

    fun pick(type: MediaType, callback: (List<Uri>) -> Unit) {
        val mimeType = when (type) {
            MediaType.IMAGE -> "image/*"
            MediaType.VIDEO -> "video/*"
            MediaType.AUDIO -> "audio/*"
            MediaType.ALL -> "*/*"
        }

        resultManager.pick((Intent(Intent.ACTION_GET_CONTENT).apply {
            this.type = mimeType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }),callback)
    }

    fun capturePhoto(callback: (Uri?) -> Unit) {
        resultManager.capture(Intent(MediaStore.ACTION_IMAGE_CAPTURE),callback)
    }
}


