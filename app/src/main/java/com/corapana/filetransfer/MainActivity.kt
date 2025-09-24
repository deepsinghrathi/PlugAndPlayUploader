package com.corapana.filetransfer

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import java.io.File

class MainActivity : BaseResultAppCompatActivity(), TransferListener {

    lateinit var destinationFile: File
    lateinit var fileTransfer: FileTransfer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        destinationFile = File(getExternalFilesDir(null), "sample.pdf")

        // Simple layout with 2 buttons
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val downloadBtn = Button(this@MainActivity).apply {
                text = "Download Test File"
                setOnClickListener {
                    testDownload()
                }
            }
            val uploadBtn = Button(this@MainActivity).apply {
                text = "Upload Test File"
                setOnClickListener {
                    testUpload()
                }
            }
            addView(downloadBtn)
            addView(uploadBtn)
        }

        setContentView(layout)


        fileTransfer = FileTransfer.from(this)
    }

    private fun testDownload() {
        fileTransfer.download(
            context = this,
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            destination = destinationFile,
            listener = this,
            rationaleMessage = "Storage permission is required to download files"
        )
    }

    private fun testUpload() {
        fileTransfer.fetchMedia(
            context = this,
            type = MediaType.IMAGE,
            url = "https://httpbin.org/post", // test server that echoes back
            method = HttpMethod.POST,
            extraFields = mapOf("userId" to "123"),
            listener = this
        )
    }

    // ---------------- TransferListener Callbacks ----------------

    override fun onProgress(id: String, progress: Int) {
        Log.d("FileTransfer", "[$id] Progress: $progress%")
    }

    override fun onComplete(id: String, response: String) {
        Log.d("FileTransfer", "[$id] Completed -> $response")
        Toast.makeText(this, "Completed: $response", Toast.LENGTH_LONG).show()
    }

    override fun onError(id: String, error: Throwable) {
        Log.e("FileTransfer", "[$id] Error", error)
        Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
    }
}