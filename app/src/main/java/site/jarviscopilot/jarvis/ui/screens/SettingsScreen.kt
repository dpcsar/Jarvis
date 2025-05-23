package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.components.*
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    // State for settings
    var useVoiceControl by remember { mutableStateOf(true) }
    var useNightMode by remember { mutableStateOf(false) }
    var showAudioPermissionDialog by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column {
                TopRibbon()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Voice Control Setting
            SettingSwitchItem(
                title = "Voice Control",
                description = "Enable voice commands and responses",
                icon = if (useVoiceControl) Icons.Default.Mic else Icons.Default.MicOff,
                isChecked = useVoiceControl,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        // Would need permission check here in real implementation
                        showAudioPermissionDialog = true
                    } else {
                        useVoiceControl = false
                        toastMessage = "Voice control disabled"
                        showToast = true
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Night Mode Setting
            SettingSwitchItem(
                title = "Night Mode",
                description = "Enable dark theme optimized for night flying",
                icon = Icons.Default.NightlightRound,
                isChecked = useNightMode,
                onCheckedChange = { isChecked ->
                    useNightMode = isChecked
                    toastMessage = if (isChecked) "Night mode enabled" else "Night mode disabled"
                    showToast = true
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Voice Training Section
            SettingsSectionHeader(title = "Voice Training")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Train the 'Hey Jarvis' wake word to better recognize your voice",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            JarvisButton(
                onClick = {
                    toastMessage = "Voice training started"
                    showToast = true
                }
            ) {
                Text("Start Voice Training")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Checklist Import Section
            SettingsSectionHeader(title = "Checklist Management")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Import custom checklists from JSON file",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            JarvisButton(
                onClick = {
                    toastMessage = "Checklist import started"
                    showToast = true
                }
            ) {
                Text("Import Checklist")
            }
        }

        // Audio Permission Dialog
        if (showAudioPermissionDialog) {
            JarvisConfirmationDialog(
                title = "Audio Permission Required",
                message = "Voice control requires microphone access. Would you like to grant this permission?",
                onConfirmClick = {
                    // In real implementation, would request permission here
                    useVoiceControl = true
                    showAudioPermissionDialog = false
                    toastMessage = "Voice control enabled"
                    showToast = true
                },
                onDismissClick = {
                    showAudioPermissionDialog = false
                },
                onDismissRequest = {
                    showAudioPermissionDialog = false
                },
                confirmText = "Grant",
                dismissText = "Cancel"
            )
        }

        // Custom Toast
        JarvisToast(
            message = toastMessage,
            isShowing = showToast,
            onDismiss = { showToast = false }
        )
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// Preview composable that shows the UI in both light and dark modes
@Preview(
    name = "Light Mode",
    apiLevel = 35,
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    apiLevel = 35,
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettingsScreenPreviewDark() {
    SettingsScreen(onNavigateBack =  {})
}
