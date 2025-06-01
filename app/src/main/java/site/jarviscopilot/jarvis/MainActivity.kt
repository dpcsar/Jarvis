package site.jarviscopilot.jarvis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import site.jarviscopilot.jarvis.ui.navigation.JarvisNavHost
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.util.ThemeMode
import site.jarviscopilot.jarvis.util.UserPreferences

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JarvisApp()
        }
    }
}

@Composable
fun JarvisApp() {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences.getInstance(context) }

    // Collect the theme mode from UserPreferences
    val themeMode =
        userPreferences.themeModeFlow.collectAsState(initial = userPreferences.getThemeMode())

    // Determine if dark theme should be used based on the selected theme mode
    val isDarkTheme = when (themeMode.value) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    JarvisTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        Scaffold { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = JarvisTheme.colorScheme.background,
                contentColor = JarvisTheme.colorScheme.onBackground
            ) {
                // No longer need to pass repository and state manager dependencies
                JarvisNavHost(
                    navController = navController
                )
            }
        }
    }
}

