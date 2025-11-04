package com.corapana.filetransfer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class TransferDialog(context: Context) : TransferListener {

    private val dialog: AlertDialog
    private val container: LinearLayout
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val items = mutableMapOf<String, View>()

    init {
        val root = inflater.inflate(R.layout.dialog_transfer, null)
        container = root.findViewById(R.id.transferList)

        dialog = AlertDialog.Builder(context)
            .setView(root)
            .setCancelable(false)
            .create()
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    override fun onProgress(id: String, progress: Int) {
        val item = items[id] ?: addItem(id)
        val progressBar = item.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = item.findViewById<TextView>(R.id.progressText)

        progressBar.progress = progress
        progressText.text = "$progress%"
    }

    override fun onComplete(id: String, response: String) {
        val item = items[id] ?: return
        val progressText = item.findViewById<TextView>(R.id.progressText)
        progressText.text = "✔"
    }

    override fun onError(id: String, error: Throwable) {
        val item = items[id] ?: return
        val progressText = item.findViewById<TextView>(R.id.progressText)
        progressText.text = "❌"
    }

    private fun addItem(id: String): View {
        val view = inflater.inflate(R.layout.view_transfer_item, container, false)
        val fileName = view.findViewById<TextView>(R.id.fileName)
        fileName.text = Uri.parse(id).lastPathSegment ?: "file"

        container.addView(view)
        items[id] = view
        return view
    }
}