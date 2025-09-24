package com.corapana.filetransfer

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.corapana.filetransfer.ui.ProgressDialogManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

object DownloadManager {

    private val client = OkHttpClient()
    private val activeCalls = ConcurrentHashMap<String, Call>()
    private val lastRequests = ConcurrentHashMap<String, DownloadRequest>()


    fun startDownload(
        context: Context,
        urls: List<String>,
        destination: File,
        listener: TransferListener
    ) {
        for (url in urls) {
            val id = url
            lastRequests[id] = DownloadRequest(context, url, destination, listener)
            ProgressDialogManager.show(context)
            val mainHandler = Handler(Looper.getMainLooper())

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val request = Request.Builder().url(url).build()
                    val call = client.newCall(request)
                    activeCalls[id] = call
                    val response = call.execute()

                    if (!response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            activeCalls.remove(id)
                            listener.onError(id, Exception("Download failed"))
                            ProgressDialogManager.updateProgress(id, 0, "Failed: Download failed")
                        }
                        return@launch
                    }

                    val body = response.body ?: run {
                        withContext(Dispatchers.Main) {
                            activeCalls.remove(id)
                            listener.onError(id, Exception("Empty response body"))
                            ProgressDialogManager.updateProgress(
                                id,
                                0,
                                "Failed: Empty response body"
                            )
                        }
                        return@launch
                    }

                    val totalBytes = body.contentLength()
                    var downloadedBytes: Long = 0

                    val inputStream: InputStream = body.byteStream()
                    val outputStream = FileOutputStream(destination)

                    withContext(Dispatchers.Main) {
                        ProgressDialogManager.addTransfer(id, destination.name)
                    }

                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var read: Int

                    inputStream.use { input ->
                        outputStream.use { output ->
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloadedBytes += read

                                // Report progress on main thread
                                val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                                mainHandler.post {
                                    listener.onProgress(id, progress)
                                    ProgressDialogManager.updateProgress(id, progress, "${progress}% downloading...")
                                }
                            }
                            output.flush()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        activeCalls.remove(id)
                        listener.onComplete(id, destination.absolutePath)
                        ProgressDialogManager.updateProgress(id, 100, "Completed")
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

    fun startDownload(
        context: Context,
        url: String,
        destination: File,
        listener: TransferListener
    ) {
        val id = url
        lastRequests[id] = DownloadRequest(context, url, destination, listener)
        ProgressDialogManager.show(context)
        val mainHandler = Handler(Looper.getMainLooper())

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val call = client.newCall(request)
                activeCalls[id] = call
                val response = call.execute()

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        activeCalls.remove(id)
                        listener.onError(id, Exception("Download failed"))
                        ProgressDialogManager.updateProgress(id, 0, "Failed: Download failed")
                    }
                    return@launch
                }

                val body = response.body ?: run {
                    withContext(Dispatchers.Main) {
                        activeCalls.remove(id)
                        listener.onError(id, Exception("Empty response body"))
                        ProgressDialogManager.updateProgress(
                            id,
                            0,
                            "Failed: Empty response body"
                        )
                    }
                    return@launch
                }

                val totalBytes = body.contentLength()
                var downloadedBytes: Long = 0

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(destination)

                withContext(Dispatchers.Main) {
                    ProgressDialogManager.addTransfer(id, destination.name)
                }

                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read: Int

                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloadedBytes += read

                            // Report progress on main thread
                            val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                            mainHandler.post {
                                listener.onProgress(id, progress)
                                ProgressDialogManager.updateProgress(id, progress,"${progress}% downloading...")
                            }
                        }
                        output.flush()
                    }
                }

                withContext(Dispatchers.Main) {
                    activeCalls.remove(id)
                    listener.onComplete(id, destination.absolutePath)
                    ProgressDialogManager.updateProgress(id, 100, "Completed")
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

    private const val DEFAULT_BUFFER_SIZE = 8 * 1024


    fun cancelDownload(id: String) {
        activeCalls[id]?.cancel()
        activeCalls.remove(id)
        ProgressDialogManager.updateProgress(id, 0, "Cancelled")
    }

    fun retryDownload(
        activity: Activity,
        url: String,
        saveDir: File,
        listener: TransferListener
    ) {
        startDownload(activity, listOf(url), saveDir, listener)
    }

    fun retryLast(id: String) {
        lastRequests[id]?.let {
            startDownload(it.context, it.url, it.saveDir, it.listener)
        }
    }

}

