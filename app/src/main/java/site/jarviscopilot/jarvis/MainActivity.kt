package site.jarviscopilot.jarvis

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
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

        Scaffold { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                // Handle permissions using the PermissionHandler utility
                if (activity != null && !PermissionHandler.hasAudioPermission(activity)) {
                    RequestAudioPermission(
                        onPermissionGranted = {
                            Toast.makeText(activity, "Audio recording permission granted", Toast.LENGTH_SHORT).show()
                        },
                        onPermissionDenied = {
                            Toast.makeText(activity, "Audio recording permission denied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Use the JarvisNavHost for navigation
                JarvisNavHost(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true, name = "Jarvis App Preview")
@Composable
fun JarvisAppPreview() {
    JarvisApp() // Preview without activity context
}
