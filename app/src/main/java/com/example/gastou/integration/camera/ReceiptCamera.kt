package com.example.gastou.integration.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/**
 * Integración de cámara para fotografiar el ticket de un gasto.
 * Pide el permiso en el momento de uso y guarda la foto en el almacenamiento interno
 * de la app (files/receipts), que no se borra al cerrar la app como la caché.
 *
 * Uso: val takePhoto = rememberReceiptCamera { uri -> ... }; takePhoto()
 */
@Composable
fun rememberReceiptCamera(onPhotoTaken: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingUri by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            onPhotoTaken(uri)
        }
        pendingUri = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createReceiptUri(context)
            pendingUri = uri.toString()
            cameraLauncher.launch(uri)
        } else {
            // Sin permiso la app sigue funcionando; solo no se adjunta la foto del ticket
            Toast.makeText(
                context,
                "Se necesita el permiso de cámara para fotografiar el ticket",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    return remember(permissionLauncher) {
        { permissionLauncher.launch(Manifest.permission.CAMERA) }
    }
}

private fun createReceiptUri(context: Context): Uri {
    val directory = File(context.filesDir, "receipts").apply { mkdirs() }
    val file = File(directory, "receipt_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
