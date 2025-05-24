package site.jarviscopilot.jarvis.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import site.jarviscopilot.jarvis.ui.components.JarvisButton
import site.jarviscopilot.jarvis.ui.components.JarvisDialog
import site.jarviscopilot.jarvis.ui.components.JarvisOutlinedButton

object PermissionHandler {
    fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun RequestAudioPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    if (PermissionHandler.hasAudioPermission(context)) {
        LaunchedEffect(key1 = Unit) {
            onPermissionGranted()
        }
        return
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            showRationale = true
            onPermissionDenied()
        }
    }

    LaunchedEffect(key1 = Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    if (showRationale) {
        JarvisDialog(
            title = "Microphone Permission Required",
            onDismissRequest = {
                showRationale = false
                onPermissionDenied()
            },
            content = {
                Text(
                    "Jarvis needs access to your microphone to enable voice control " +
                    "features. Without this permission, voice-related features will be disabled."
                )
            },
            buttons = {
                JarvisOutlinedButton(
                    onClick = {
                        showRationale = false
                        onPermissionDenied()
                    }
                ) {
                    Text("Deny")
                }
                JarvisButton(
                    onClick = {
                        showRationale = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text("Grant Permission")
                }
            }
        )
    }
}
