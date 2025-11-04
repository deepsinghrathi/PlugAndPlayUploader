package com.corapana.filetransfer.ui

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.corapana.filetransfer.R
import java.util.concurrent.ConcurrentHashMap

object ProgressDialogManager {

    private var dialog: AlertDialog? = null
    private lateinit var adapter: TransferAdapter
    private val transfers = ConcurrentHashMap<String, TransferItem>()

    // Track active transfers
    private var activeTransfers = 0

    // Callbacks
    var onCancel: ((String) -> Unit)? = null
    var onRetry: ((String) -> Unit)? = null

    fun show(context: Context) {
        if (dialog?.isShowing == true) return

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_transfer_progress, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerTransfers)
        val closeButton = view.findViewById<ImageView>(R.id.close)

        adapter = TransferAdapter(
            onCancel = { id -> onCancel?.invoke(id) },
            onRetry = { id -> onRetry?.invoke(id) }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {dismiss()}

        dialog?.show()
    }

    fun addTransfer(id: String, name: String) {
        transfers[id] = TransferItem(id, name, 0, "Starting")
        adapter.submitList(transfers.values.toList())
        activeTransfers++
    }

    fun updateProgress(id: String, progress: Int, status: String = "In Progress") {
        transfers[id]?.let {
            it.progress = progress
            it.status = status
        }
        adapter.submitList(transfers.values.toList())

        // If completed/failed/error, decrement active count
        if (status == "Completed" || status == "Failed" || status.startsWith("Error")) {
            activeTransfers--
            checkIfAllDone()
        }
    }

    private fun checkIfAllDone() {
        if (activeTransfers <= 0) {
            dismiss()
        }
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
        transfers.clear()
        activeTransfers = 0
    }

    fun removeTransfer(id: String) {
        transfers.remove(id)
        adapter.submitList(transfers.values.toList())
    }

}