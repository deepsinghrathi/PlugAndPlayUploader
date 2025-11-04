package com.corapana.filetransfer

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.corapana.filetransfer.ui.ProgressDialogManager
import com.corapana.filetransfer.utils.FileUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap

object UploadManager {
    private val client = OkHttpClient()
    private val activeCalls = ConcurrentHashMap<String, Call>()
    private val lastRequests = ConcurrentHashMap<String, UploadRequest>()

    data class UploadRequest(
        val context: Context,
        val uri: Uri,
        val url: String,
        val method: HttpMethod,
        val extraFields: Map<String, String>,
        val listener: TransferListener
    )


    @OptIn(DelicateCoroutinesApi::class)
    fun startUploads(
        context: Context,
        uris: List<Uri>,
        url: String,
        method: HttpMethod,
        extraFields: Map<String, String>,
        listener: TransferListener
    ) {
        ProgressDialogManager.show(context)
        val mainHandler = Handler(Looper.getMainLooper())

        for (uri in uris) {
            val id = uri.toString()
            lastRequests[id] = UploadRequest(context, uri, url, method, extraFields, listener)
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val file = FileUtils.fromUri(context, uri)

                    withContext(Dispatchers.Main) {
                        ProgressDialogManager.addTransfer(id, file.name)
                    }

                    val requestBody = UploadRequestBody(file, "multipart/form-data") { progress ->
                        // Switch to main thread safely
                        mainHandler.post {
                            listener.onProgress(id, progress)
                            ProgressDialogManager.updateProgress(id, progress, "${progress}% uploading...")
                        }
                    }

                    val multipart = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.name, requestBody)
                        .apply {
                            extraFields.forEach { (k, v) -> addFormDataPart(k, v) }
                        }
                        .build()

                    val request = Request.Builder()
                        .url(url)
                        .method(method.name, multipart)
                        .build()

                    val call = client.newCall(request)
                    activeCalls[id] = call
                    val response = call.execute()

                    withContext(Dispatchers.Main) {
                        activeCalls.remove(id)
                        if (response.isSuccessful) {
                            listener.onComplete(id, response.body?.string().orEmpty())
                            ProgressDialogManager.updateProgress(id, 100, "Completed")
                        } else {
                            listener.onError(id, Exception("Upload failed"))
                            ProgressDialogManager.updateProgress(id, 0, "Failed: Upload failed")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        activeCalls.remove(id)
                        listener.onError(id, e)
                        ProgressDialogManager.updateProgress(id, 0, "Failed: ${e.message}")
                    }
                }
            }
        }
    }

    fun cancelUpload(id: String) {
        activeCalls[id]?.cancel()
        activeCalls.remove(id)
        ProgressDialogManager.updateProgress(id, 0, "Cancelled")
    }

    fun retryUpload(
        context: Context,
        uri: Uri,
        url: String,
        method: HttpMethod,
        extraFields: Map<String, String>,
        listener: TransferListener
    ) {
        startUploads(context, listOf(uri), url, method, extraFields, listener)
    }

    fun retryLast(id: String) {
        lastRequests[id]?.let {
            startUploads(it.context, listOf(it.uri), it.url, it.method, it.extraFields, it.listener)
        }
    }
}
