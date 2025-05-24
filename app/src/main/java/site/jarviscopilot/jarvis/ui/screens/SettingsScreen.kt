package site.jarviscopilot.jarvis.ui.screens

import android.net.Uri
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.ChecklistInfo
import site.jarviscopilot.jarvis.data.ChecklistRepository
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
    val coroutineScope = rememberCoroutineScope()
    // Get instance of UserPreferences
    val userPreferences = remember { UserPreferences.getInstance(context) }
    // Get checklist repository
    val checklistRepository = remember { ChecklistRepository(context) }

    // State for settings - initialize from UserPreferences
    var useVoiceControl by remember { mutableStateOf(userPreferences.isVoiceControlEnabled()) }
    var themeMode by remember { mutableStateOf(userPreferences.getThemeMode()) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // State for permission handling
    var requestVoicePermission by remember { mutableStateOf(false) }

    // State for checklists
    var checklists by remember { mutableStateOf<List<ChecklistInfo>>(emptyList()) }
    var isLoadingChecklists by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var checklistToDelete by remember { mutableStateOf<ChecklistInfo?>(null) }

    // Load checklists when screen is shown
    LaunchedEffect(Unit) {
        isLoadingChecklists = true
        checklists = checklistRepository.loadAllChecklists()
        isLoadingChecklists = false
    }

    // File picker for importing checklists
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Safely request persist permissions if needed
            try {
                // Check the flags to see if permission can be persisted
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (_: Exception) {
                // Ignore permission errors - we'll still try to read the file
            }

            // Import the checklist
            coroutineScope.launch {
                val result = checklistRepository.importChecklist(it)

                result.fold(
                    onSuccess = { checklistInfo ->
                        toastMessage = "Checklist '${checklistInfo.name}' imported successfully"
                        showToast = true
                        // Reload the checklist list
                        checklists = checklistRepository.loadAllChecklists()
                    },
                    onFailure = { exception ->
                        toastMessage = "Failed to import checklist: ${exception.message}"
                        showToast = true
                    }
                )
            }
        }
    }

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

    // Confirmation dialog for deleting checklists
    if (showDeleteDialog && checklistToDelete != null) {
        // Safe-get the checklist name using let to avoid smart cast issues with delegated properties
        val checklistName = checklistToDelete?.name ?: "Unknown"

        JarvisConfirmationDialog(
            title = "Delete Checklist",
            message = "Are you sure you want to delete the checklist '$checklistName'? This action cannot be undone.",
            onConfirmClick = {
                // Get a local copy of the checklist to avoid issues with delegated properties
                val checklistToDeleteCopy = checklistToDelete

                // Only proceed if we have a valid checklist
                checklistToDeleteCopy?.let { checklist ->
                    coroutineScope.launch {
                        try {
                            val result = checklistRepository.deleteChecklist(checklist)
                            result.fold(
                                onSuccess = {
                                    toastMessage = "Checklist deleted"
                                    showToast = true

                                    // Reload checklists
                                    checklists = checklistRepository.loadAllChecklists()
                                },
                                onFailure = { exception ->
                                    toastMessage = "Failed to delete checklist: ${exception.message}"
                                    showToast = true
                                }
                            )
                        } catch (e: Exception) {
                            toastMessage = "Error deleting checklist: ${e.message}"
                            showToast = true
                        }
                    }
                }

                showDeleteDialog = false
                checklistToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                checklistToDelete = null
            },
            onDismissRequest = {
                showDeleteDialog = false
                checklistToDelete = null
            },
            confirmText = "Delete",
            dismissText = "Cancel"
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
                text = "Import custom checklists from your device or cloud storage",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            JarvisButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/json"))
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Import Checklist")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List all available checklists with delete option
            Text(
                text = "Available Checklists",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingChecklists) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (checklists.isEmpty()) {
                Text(
                    text = "No checklists available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                checklists.forEach { checklist ->
                    ChecklistListItem(
                        checklistInfo = checklist,
                        onDeleteClick = {
                            checklistToDelete = checklist
                            showDeleteDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ChecklistListItem(
    checklistInfo: ChecklistInfo,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = checklistInfo.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (checklistInfo.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = checklistInfo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (checklistInfo.isExample) "Example checklist" else "User checklist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete checklist",
                    tint = MaterialTheme.colorScheme.error
                )
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
