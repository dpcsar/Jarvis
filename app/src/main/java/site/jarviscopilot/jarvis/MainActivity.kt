package site.jarviscopilot.jarvis

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import site.jarviscopilot.jarvis.ui.screens.ChecklistScreen
import site.jarviscopilot.jarvis.ui.screens.MainScreen
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.util.PermissionHandler
import site.jarviscopilot.jarvis.util.RequestAudioPermission

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarvisTheme {
                Scaffold { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Using a simple navigation approach for now
                        var currentScreen by remember { mutableStateOf("main") }
                        var selectedChecklist by remember { mutableStateOf("") }

                        // Handle permissions using the PermissionHandler utility
                        if (!PermissionHandler.hasAudioPermission(this@MainActivity)) {
                            RequestAudioPermission(
                                onPermissionGranted = {
                                    Toast.makeText(this@MainActivity, "Audio recording permission granted", Toast.LENGTH_SHORT).show()
                                },
                                onPermissionDenied = {
                                    Toast.makeText(this@MainActivity, "Audio recording permission denied", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        when (currentScreen) {
                            "main" -> {
                                MainScreen(
                                    onChecklistSelected = { checklist ->
                                        selectedChecklist = checklist
                                        currentScreen = "checklist"
                                    },
                                    onSettingsClick = {
                                        // Settings functionality to be implemented later
                                        Toast.makeText(this@MainActivity, "Settings clicked", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            "checklist" -> {
                                ChecklistScreen(
                                    checklistName = selectedChecklist,
                                    onNavigateHome = {
                                        currentScreen = "main"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
