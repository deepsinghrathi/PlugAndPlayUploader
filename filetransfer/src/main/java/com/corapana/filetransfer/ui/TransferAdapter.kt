package com.corapana.filetransfer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.corapana.filetransfer.R

class TransferAdapter(
    private val onCancel: (String) -> Unit,
    private val onRetry: (String) -> Unit
) : RecyclerView.Adapter<TransferAdapter.TransferViewHolder>() {

    private val items = mutableListOf<TransferItem>()

    fun submitList(list: List<TransferItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transfer, parent, false)
        return TransferViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    inner class TransferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val fileName = view.findViewById<TextView>(R.id.txtFileName)
        private val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        private val status = view.findViewById<TextView>(R.id.txtStatus)
        private val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        private val btnRetry = view.findViewById<Button>(R.id.btnRetry)

        fun bind(item: TransferItem) {
            fileName.text = item.fileName
            progressBar.progress = item.progress
            status.text = item.status

            // Show retry only if failed
            btnRetry.visibility = if (item.status.contains("Failed") || item.status.contains("Error")) {
                View.VISIBLE
            } else {
                View.GONE
            }

            btnCancel.setOnClickListener {
                onCancel(item.id)
            }

            btnRetry.setOnClickListener {
                onRetry(item.id)
            }
        }
    }

    fun updateProgress(id: String, progress: Int, status: String? = null) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index].progress = progress
            status?.let { items[index].status = it }
            notifyItemChanged(index)
        }
    }

    fun addItem(item: TransferItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
}