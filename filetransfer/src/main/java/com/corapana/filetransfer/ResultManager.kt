package com.corapana.filetransfer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

interface ResultManager {
    fun pick(intent: Intent, onResult: ((List<Uri>) -> Unit)? = null)
    fun capture(intent: Intent, onResult: ((Uri?) -> Unit)? = null)
    fun requestPermissions(permissions: Array<String>, onResult: ((Boolean) -> Unit)? = null)
}

open class BaseResultAppCompatActivity :
    AppCompatActivity(), ResultManager {

    var onUrisPicked: ((List<Uri>) -> Unit)? = null
    var onUriCaptured: ((Uri?) -> Unit)? = null
    var onPermissionsResult: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var pickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = mutableListOf<Uri>()
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            }
            data?.data?.let { uris.add(it) }
        }
        onUrisPicked?.invoke(uris)
    }

    var captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = if (result.resultCode == RESULT_OK) result.data?.data else null
        onUriCaptured?.invoke(uri)
    }

    var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        onPermissionsResult?.invoke(map.values.all { it })
    }

    override fun pick(intent: Intent, onResult: ((List<Uri>) -> Unit)?) {
        if (onResult != null) onUrisPicked = onResult
        pickLauncher.launch(intent)
    }

    override fun capture(intent: Intent, onResult: ((Uri?) -> Unit)?) {
        if (onResult != null) onUriCaptured = onResult
        captureLauncher.launch(intent)
    }

    override fun requestPermissions(permissions: Array<String>, onResult: ((Boolean) -> Unit)?) {
        if (onResult != null) onPermissionsResult = onResult
        permissionLauncher.launch(permissions)
    }

}


open class BaseResultComponentActivity :
    ComponentActivity(), ResultManager {

    var onUrisPicked: ((List<Uri>) -> Unit)? = null
    var onUriCaptured: ((Uri?) -> Unit)? = null
    var onPermissionsResult: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    var pickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = mutableListOf<Uri>()
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            }
            data?.data?.let { uris.add(it) }
        }
        onUrisPicked?.invoke(uris)
    }

    var captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = if (result.resultCode == RESULT_OK) result.data?.data else null
        onUriCaptured?.invoke(uri)
    }

    var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        onPermissionsResult?.invoke(map.values.all { it })
    }

    override fun pick(intent: Intent, onResult: ((List<Uri>) -> Unit)?) {
        if (onResult != null) onUrisPicked = onResult
        pickLauncher.launch(intent)
    }

    override fun capture(intent: Intent, onResult: ((Uri?) -> Unit)?) {
        if (onResult != null) onUriCaptured = onResult
        captureLauncher.launch(intent)
    }

    override fun requestPermissions(permissions: Array<String>, onResult: ((Boolean) -> Unit)?) {
        if (onResult != null) onPermissionsResult = onResult
        permissionLauncher.launch(permissions)
    }
}


open class BaseResultFragment :
    Fragment(), ResultManager {

    var onUrisPicked: ((List<Uri>) -> Unit)? = null
    var onUriCaptured: ((Uri?) -> Unit)? = null
    var onPermissionsResult: ((Boolean) -> Unit)? = null


    var pickLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = mutableListOf<Uri>()
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            }
            data?.data?.let { uris.add(it) }
        }
        onUrisPicked?.invoke(uris)
    }

    var captureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = if (result.resultCode == Activity.RESULT_OK) result.data?.data else null
        onUriCaptured?.invoke(uri)
    }

    var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        onPermissionsResult?.invoke(map.values.all { it })
    }

    override fun pick(intent: Intent, onResult: ((List<Uri>) -> Unit)?) {
        if (onResult != null) onUrisPicked = onResult
        pickLauncher.launch(intent)
    }

    override fun capture(intent: Intent, onResult: ((Uri?) -> Unit)?) {
        if (onResult != null) onUriCaptured = onResult
        captureLauncher.launch(intent)
    }

    override fun requestPermissions(permissions: Array<String>, onResult: ((Boolean) -> Unit)?) {
        if (onResult != null) onPermissionsResult = onResult
        permissionLauncher.launch(permissions)
    }
}
