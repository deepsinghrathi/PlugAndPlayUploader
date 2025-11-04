package com.corapana.filetransfer

import android.app.Activity
import android.content.Context
import java.io.File

data class DownloadRequest(
    val context: Context,
    val url: String,
    val saveDir: File,
    val listener: TransferListener
)