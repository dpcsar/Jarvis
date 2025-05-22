package site.jarviscopilot.jarvis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import site.jarviscopilot.jarvis.ui.screens.ChecklistScreen
import site.jarviscopilot.jarvis.ui.screens.MainScreen
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Audio recording permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request microphone permission
        requestMicrophonePermission()

        setContent {
            JarvisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Using a simple navigation approach for now
                    var currentScreen by remember { mutableStateOf("main") }
                    var selectedChecklist by remember { mutableStateOf("") }

                    when (currentScreen) {
                        "main" -> {
                            MainScreen(
                                onChecklistSelected = { checklist ->
                                    selectedChecklist = checklist
                                    currentScreen = "checklist"
                                },
                                onSettingsClick = {
                                    // Settings functionality to be implemented later
                                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
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

    private fun requestMicrophonePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                // Show rationale if needed
                Toast.makeText(
                    this,
                    "Microphone permission is needed for voice commands",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}

