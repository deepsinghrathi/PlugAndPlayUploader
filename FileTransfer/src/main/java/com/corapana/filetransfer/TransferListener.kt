package com.corapana.filetransfer

interface TransferListener {
    fun onProgress(id: String, progress: Int)
    fun onComplete(id: String, response: String)
    fun onError(id: String, error: Throwable)
}