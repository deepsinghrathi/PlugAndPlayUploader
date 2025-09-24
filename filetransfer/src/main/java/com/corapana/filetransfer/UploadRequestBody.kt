package com.corapana.filetransfer

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class UploadRequestBody(
    private val file: File,
    private val contentType: String,
    private val listener: (progress: Int) -> Unit
) : RequestBody() {

    override fun contentType(): okhttp3.MediaType? {
        return contentType.toMediaTypeOrNull()
    }
    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val length = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val input = FileInputStream(file)
        var uploaded: Long = 0

        input.use {
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                listener(((uploaded * 100) / length).toInt())
                uploaded += read
                sink.write(buffer, 0, read)
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}

