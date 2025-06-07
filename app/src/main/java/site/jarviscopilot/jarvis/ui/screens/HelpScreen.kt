package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JarvisTheme.colorScheme.primaryContainer,
                    titleContentColor = JarvisTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = JarvisTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HelpSection(
                title = "Getting Started",
                content = "Welcome to the Jarvis Aviation Assistant. This app helps pilots manage checklists and flight information efficiently."
            )

            HelpSection(
                title = "Using Checklists",
                content = "1. Select a checklist from the main screen\n" +
                        "2. Follow each step and mark them as complete\n" +
                        "3. You can resume a checklist where you left off or restart it from the beginning"
            )

            HelpSection(
                title = "Advanced Features",
                content = "• Tap on a checklist title to have the app read the list aloud\n" +
                        "• Long press on a checklist in the list ribbon to mark it as complete\n" +
                        "• These shortcuts help you navigate through checklists more efficiently during flight operations"
            )

            HelpSection(
                title = "Flight Information",
                content = "The app displays current local and UTC time at the top of the screen. Your active flight plan (if any) will also be displayed in the top bar."
            )

            HelpSection(
                title = "Settings",
                content = "Access settings from the main screen by tapping the gear icon. Here you can adjust display preferences and app behavior."
            )

            HelpSection(
                title = "Support",
                content = "If you need assistance, please contact support at support@jarviscopilot.site"
            )
        }
    }
}

@Composable
private fun HelpSection(title: String, content: String) {
    Text(
        text = title,
        style = JarvisTheme.typography.titleLarge,
        color = JarvisTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        text = content,
        style = JarvisTheme.typography.bodyMedium,
        color = JarvisTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))
}
