package site.jarviscopilot.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import site.jarviscopilot.jarvis.ui.components.JarvisToast
import site.jarviscopilot.jarvis.ui.navigation.JarvisNavHost
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.util.PermissionHandler
import site.jarviscopilot.jarvis.util.RequestAudioPermission

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarvisApp(activity = this)
        }
    }
}

@Composable
fun JarvisApp(activity: ComponentActivity? = null) {
    JarvisTheme {
        val navController = rememberNavController()
        val showToast = remember { mutableStateOf(false) }
        val toastMessage = remember { mutableStateOf("") }

        if (activity != null && !PermissionHandler.hasAudioPermission(activity)) {
            RequestAudioPermission(
                onPermissionGranted = {
                    toastMessage.value = "Audio recording permission granted"
                    showToast.value = true
                },
                onPermissionDenied = {
                    toastMessage.value = "Audio recording permission denied"
                    showToast.value = true
                }
            )
        }

        // Custom Jarvis-themed toast - only show when showToast is true
        if (showToast.value) {
            JarvisToast(
                message = toastMessage.value,
                onDismiss = { showToast.value = false }
            )
        }

        Scaffold { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                // Use the JarvisNavHost for navigation
                JarvisNavHost(navController = navController)
            }
        }
    }
}

// Preview composable that shows the UI in both light and dark modes
@Preview( name = "Light Mode", apiLevel = 35, showBackground = true )
@Preview( name = "Dark Mode", apiLevel = 35, showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun JarvisAppPreviewDark() {
    JarvisTheme {
        JarvisApp()
    }
}
