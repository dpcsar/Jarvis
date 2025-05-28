package site.jarviscopilot.jarvis.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.model.ChecklistInfoData
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.ui.components.JarvisButton
import site.jarviscopilot.jarvis.ui.components.JarvisConfirmationDialog
import site.jarviscopilot.jarvis.ui.components.JarvisToast
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.util.PermissionHandler
import site.jarviscopilot.jarvis.util.RequestAudioPermission
import site.jarviscopilot.jarvis.util.ThemeMode
import site.jarviscopilot.jarvis.util.UserPreferences

@Composable
fun SettingsScreen(
    checklistRepository: IChecklistRepository, // Add parameter for repository
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Get instance of UserPreferences
    val userPreferences = remember { UserPreferences.getInstance(context) }
    // State for settings - initialize from UserPreferences
    var useVoiceControl by remember { mutableStateOf(userPreferences.isVoiceControlEnabled()) }
    var themeMode by remember { mutableStateOf(userPreferences.getThemeMode()) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    // State for permission handling
    var requestVoicePermission by remember { mutableStateOf(false) }
    // State for checklists
    var checklists by remember { mutableStateOf<List<ChecklistInfoData>>(emptyList()) }
    var isLoadingChecklists by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var checklistToDelete by remember { mutableStateOf<ChecklistInfoData?>(null) }

    // Load checklists when screen is shown
    LaunchedEffect(Unit) {
        isLoadingChecklists = true
        checklists = checklistRepository.getAvailableChecklists()
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
                        checklists = checklistRepository.getAvailableChecklists()
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
                            val success = checklistRepository.deleteChecklist(checklist.id)
                            if (success) {
                                toastMessage = "Checklist deleted"
                                showToast = true

                                // Reload checklists
                                checklists = checklistRepository.getAvailableChecklists()
                            } else {
                                toastMessage = "Failed to delete checklist"
                                showToast = true
                            }
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
                TopBar()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = JarvisTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "Settings",
                        style = JarvisTheme.typography.titleLarge,
                        color = JarvisTheme.colorScheme.onPrimaryContainer,
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
                .background(JarvisTheme.colorScheme.background)
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
                style = JarvisTheme.typography.bodyMedium,
                color = JarvisTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            ThemeSelectionItem(
                selectedTheme = themeMode,
                onThemeSelected = { newThemeMode ->
                    themeMode = newThemeMode
                    userPreferences.setThemeMode(newThemeMode)
                    val themeName = when (newThemeMode) {
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
                style = JarvisTheme.typography.bodyMedium,
                color = JarvisTheme.colorScheme.onBackground
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
                style = JarvisTheme.typography.bodyMedium,
                color = JarvisTheme.colorScheme.onBackground
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
                style = JarvisTheme.typography.titleMedium,
                color = JarvisTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingChecklists) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (checklists.isEmpty()) {
                Text(
                    text = "No checklists available",
                    style = JarvisTheme.typography.bodyMedium,
                    color = JarvisTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                checklists.forEach { checklist ->
                    ChecklistListItem(
                        checklistInfoData = checklist,
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
    checklistInfoData: ChecklistInfoData,
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
                    text = checklistInfoData.name,
                    style = JarvisTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (checklistInfoData.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = checklistInfoData.description,
                        style = JarvisTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (checklistInfoData.isExample) "Example checklist" else "User checklist",
                    style = JarvisTheme.typography.bodySmall,
                    color = JarvisTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete checklist",
                    tint = JarvisTheme.colorScheme.emergency
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
            tint = JarvisTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = JarvisTheme.typography.titleMedium,
                color = JarvisTheme.colorScheme.onBackground
            )

            Text(
                text = description,
                style = JarvisTheme.typography.bodyMedium,
                color = JarvisTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = JarvisTheme.colorScheme.primary,
                checkedTrackColor = JarvisTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = JarvisTheme.colorScheme.surfaceVariant,
                uncheckedTrackColor = JarvisTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = JarvisTheme.typography.titleLarge,
        color = JarvisTheme.colorScheme.primary,
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
        JarvisTheme.colorScheme.primaryContainer
    } else {
        JarvisTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        JarvisTheme.colorScheme.primary
    } else {
        JarvisTheme.colorScheme.outline
    }

    val contentColor = if (isSelected) {
        JarvisTheme.colorScheme.onPrimaryContainer
    } else {
        JarvisTheme.colorScheme.onSurface
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
            style = JarvisTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

