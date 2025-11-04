package com.corapana.filetransfer.ui

data class TransferItem(
    val id: String,
    var fileName: String,
    var progress: Int = 0,
    var status: String = "Pending"
)