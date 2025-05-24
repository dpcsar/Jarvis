package site.jarviscopilot.jarvis.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun shouldShowRationale(context: Context, permission: String): Boolean {
        return androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
            context as androidx.activity.ComponentActivity,
            permission
        )
    }
}

@Composable
fun RequestAudioPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

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
            // Check if we can show the permission dialog again or need to direct to settings
            if (PermissionHandler.shouldShowRationale(context, Manifest.permission.RECORD_AUDIO)) {
                showRationale = true
            } else {
                showSettings = true
            }
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

    if (showSettings) {
        JarvisDialog(
            title = "Permission Required",
            onDismissRequest = {
                showSettings = false
                onPermissionDenied()
            },
            content = {
                Text(
                    "Microphone permission is required but has been permanently denied. " +
                    "Please open app settings and manually grant the microphone permission."
                )
            },
            buttons = {
                JarvisOutlinedButton(
                    onClick = {
                        showSettings = false
                        onPermissionDenied()
                    }
                ) {
                    Text("Cancel")
                }
                JarvisButton(
                    onClick = {
                        showSettings = false
                        PermissionHandler.openAppSettings(context)
                    }
                ) {
                    Text("Open Settings")
                }
            }
        )
    }
}
