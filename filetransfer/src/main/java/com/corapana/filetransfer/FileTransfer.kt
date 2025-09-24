package com.corapana.filetransfer

import android.content.Context
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.fragment.app.Fragment
import com.corapana.filetransfer.ui.ProgressDialogManager
import java.io.File

object FileTransfer {

    /**
     * Fetch media from gallery (image/video/audio/all) and upload.
     */
    fun fetchMedia(
        context: Context,
        type: MediaType = MediaType.ALL,
        url: String,
        method: HttpMethod = HttpMethod.POST,
        extraFields: Map<String, String> = emptyMap(),
        rationaleMessage: String? = null,
        listener: TransferListener? = null
    ) {
        val dialog = listener ?: TransferDialog(context).apply { show() }

        PermissionManager.checkAndRequest(
            context,
            PermissionRequester(resultManager),
            PermissionHelper.getStoragePermissions(),
            rationaleMessage
        ) { granted ->
            if (granted) {
                MediaPicker(resultManager).pick(type) { uris ->
                    UploadManager.startUploads(
                        context,
                        uris,
                        url,
                        method,
                        extraFields,
                        dialog
                    )
                }
            } else {
                PermissionHelper.showPermissionDialog(context)
            }
        }
    }

    /**
     * Capture photo using camera and upload.
     */
    fun capturePhoto(
        context: Context,
        url: String,
        method: HttpMethod = HttpMethod.POST,
        extraFields: Map<String, String> = emptyMap(),
        rationaleMessage: String? = null,
        listener: TransferListener
    ) {
        PermissionManager.checkAndRequest(
            context,
            PermissionRequester(resultManager),
            PermissionHelper.getCameraPermission(),
            rationaleMessage
        ) { granted ->
            if (granted) {
                MediaPicker(resultManager).capturePhoto { uri ->
                    uri?.let {
                        it
                        UploadManager.startUploads(
                            context,
                            listOf(it),
                            url,
                            method,
                            extraFields,
                            listener
                        )
                    }

                }
            } else {
                PermissionHelper.showPermissionDialog(context)
            }
        }
    }

    fun from(any: Any): FileTransfer {
        resultManager = when (any) {
            is ResultManager -> any
            is Fragment -> error("${any::class.java.simpleName} must extend BaseResultFragment")
            is ComponentActivity -> error("${any::class.java.simpleName} must extend BaseResultComponentActivity or BaseResultAppCompatActivity")
            else -> error("${any::class.java.simpleName} is not a valid host")
        }
        init()
        return this
    }


    /**
     * Initialize library if needed (future use: logging, config, etc.)
     */
    private var isInitialized = false
    private lateinit var resultManager: ResultManager

    fun init() {
        if (isInitialized) return
        isInitialized = true

        ProgressDialogManager.onCancel = { id ->
            if (id.startsWith("http")) {
                DownloadManager.cancelDownload(id)
            } else {
                UploadManager.cancelUpload(id)
            }
            ProgressDialogManager.removeTransfer(id)
        }

        ProgressDialogManager.onRetry = { id ->
            if (id.startsWith("http")) {
                // Retry download
                DownloadManager.retryLast(id)
            } else {
                // Retry upload
                UploadManager.retryLast(id)
            }
        }
    }

    /**
     * Upload one or multiple files.
     */
    fun upload(
        context: Context,
        uris: List<Uri>,
        url: String,
        method: HttpMethod = HttpMethod.POST,
        extraFields: Map<String, String> = emptyMap(),
        listener: TransferListener
    ) {
        UploadManager.startUploads(
            context = context,
            uris = uris,
            url = url,
            method = method,
            extraFields = extraFields,
            listener = listener
        )
    }

    /**
     * Upload a single file for convenience.
     */
    fun upload(
        context: Context,
        uri: Uri,
        url: String,
        method: HttpMethod = HttpMethod.POST,
        extraFields: Map<String, String> = emptyMap(),
        listener: TransferListener
    ) {
        upload(context, listOf(uri), url, method, extraFields, listener)
    }

    /**
     * Download a file from URL.
     */
    fun download(
        context: Context,
        url: String,
        destination: File,
        listener: TransferListener
    ) {
        DownloadManager.startDownload(
            context = context,
            url = url,
            destination = destination,
            listener = listener
        )
    }

    fun upload(
        context: Context,
        uris: List<Uri>,
        url: String,
        method: HttpMethod = HttpMethod.POST,
        extraFields: Map<String, String> = emptyMap(),
        listener: TransferListener,
        rationaleMessage: String? = null
    ) {
        PermissionManager.checkAndRequest(
            context,
            PermissionRequester(resultManager),
            PermissionHelper.getStoragePermissions(),
            rationaleMessage
        ) { granted ->
            if (granted) {
                UploadManager.startUploads(context, uris, url, method, extraFields, listener)
            } else {
                listener.onError("permissions", Exception("Permissions denied"))
            }
        }
    }

    fun download(
        context: Context,
        url: String,
        destination: File,
        listener: TransferListener,
        rationaleMessage: String? = null
    ) {
        PermissionManager.checkAndRequest(
            context,
            PermissionRequester(resultManager),
            PermissionHelper.getStoragePermissions(),
            rationaleMessage
        ) { granted ->
            if (granted) {
                DownloadManager.startDownload(context, url, destination, listener)
            } else {
                listener.onError("permissions", Exception("Permissions denied"))
            }
        }
    }

}