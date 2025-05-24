package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.components.*
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.util.PermissionHandler
import site.jarviscopilot.jarvis.util.RequestAudioPermission
import site.jarviscopilot.jarvis.util.UserPreferences
import site.jarviscopilot.jarvis.util.ThemeMode

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    // Get instance of UserPreferences
    val userPreferences = remember { UserPreferences.getInstance(context) }

    // State for settings - initialize from UserPreferences
    var useVoiceControl by remember { mutableStateOf(userPreferences.isVoiceControlEnabled()) }
    var themeMode by remember { mutableStateOf(userPreferences.getThemeMode()) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // State for permission handling
    var requestVoicePermission by remember { mutableStateOf(false) }

    // Custom Jarvis-themed toast - only show when showToast is true
    if (showToast) {
        JarvisToast(
            message = toastMessage,
            onDismiss = { showToast = false }
        )
    }

    // Permission request handling
    if (requestVoicePermission) {
        RequestAudioPermission(
            onPermissionGranted = {
                useVoiceControl = true
                userPreferences.setVoiceControlEnabled(true)
                toastMessage = "Voice control enabled"
                showToast = true
                requestVoicePermission = false
            },
            onPermissionDenied = {
                useVoiceControl = false
                userPreferences.setVoiceControlEnabled(false)
                toastMessage = "Voice control disabled - microphone permission required"
                showToast = true
                requestVoicePermission = false
            }
        )
    }

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
                        // Check for audio permission before enabling voice control
                        if (PermissionHandler.hasAudioPermission(context)) {
                            useVoiceControl = true
                            userPreferences.setVoiceControlEnabled(true)
                            toastMessage = "Voice control enabled"
                            showToast = true
                        } else {
                            // Request permission if not granted
                            requestVoicePermission = true
                        }
                    } else {
                        useVoiceControl = false
                        userPreferences.setVoiceControlEnabled(false)
                        toastMessage = "Voice control disabled"
                        showToast = true
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Theme Mode Selection
            SettingsSectionHeader(title = "Theme")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose your preferred theme mode",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            ThemeSelectionItem(
                selectedTheme = themeMode,
                onThemeSelected = { newThemeMode ->
                    themeMode = newThemeMode
                    userPreferences.setThemeMode(newThemeMode)
                    val themeName = when(newThemeMode) {
                        ThemeMode.SYSTEM -> "System default"
                        ThemeMode.LIGHT -> "Light mode"
                        ThemeMode.DARK -> "Dark mode"
                    }
                    toastMessage = "$themeName selected"
                    showToast = true
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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

@Composable
fun ThemeSelectionItem(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // System Theme Option
        ThemeOption(
            title = "System",
            icon = Icons.Default.BrightnessAuto,
            isSelected = selectedTheme == ThemeMode.SYSTEM,
            onClick = { onThemeSelected(ThemeMode.SYSTEM) },
            modifier = Modifier.weight(1f)
        )

        // Light Theme Option
        ThemeOption(
            title = "Light",
            icon = Icons.Default.WbSunny,
            isSelected = selectedTheme == ThemeMode.LIGHT,
            onClick = { onThemeSelected(ThemeMode.LIGHT) },
            modifier = Modifier.weight(1f)
        )

        // Dark Theme Option
        ThemeOption(
            title = "Dark",
            icon = Icons.Default.NightlightRound,
            isSelected = selectedTheme == ThemeMode.DARK,
            onClick = { onThemeSelected(ThemeMode.DARK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$title theme",
            tint = contentColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

// Preview composable that shows the UI in both light and dark modes
@Preview( name = "Light Mode", apiLevel = 35, showBackground = true )
@Composable
fun SettingsScreenLightPreview() {
    JarvisTheme {
        SettingsScreen(onNavigateBack =  {})
    }
}

@Preview( name = "Dark Mode", apiLevel = 35, showBackground = true )
@Composable
fun SettingsScreenDarkPreview() {
    JarvisTheme (darkTheme = true) {
        SettingsScreen(onNavigateBack =  {})
    }
}
