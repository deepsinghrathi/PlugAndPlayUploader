package com.corapana.filetransfer

class PermissionRequester(private val resultManager: ResultManager) {

    fun requestPermissions(permissions: Array<String>, callback: ((Boolean) -> Unit)?) {
        resultManager.requestPermissions(permissions, callback)
    }

}

