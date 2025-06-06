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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import site.jarviscopilot.jarvis.data.model.ChecklistInfoData
import site.jarviscopilot.jarvis.di.AppDependencies
import site.jarviscopilot.jarvis.ui.components.JarvisButton
import site.jarviscopilot.jarvis.ui.components.JarvisConfirmationDialog
import site.jarviscopilot.jarvis.ui.components.JarvisToast
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.util.PermissionHandler
import site.jarviscopilot.jarvis.util.RequestAudioPermission
import site.jarviscopilot.jarvis.util.ThemeMode
import site.jarviscopilot.jarvis.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModelFactory = AppDependencies.provideSettingsViewModelFactory(context)
    val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)

    // Collect ViewModel states
    val checklists by viewModel.checklists.collectAsState()
    val isLoadingChecklists by viewModel.isLoading.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val useVoiceControl by viewModel.useVoiceControl.collectAsState()
    val useTTS by viewModel.useTts.collectAsState()
    val toastEvent by viewModel.toastEvent.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var checklistToDelete by remember { mutableStateOf<ChecklistInfoData?>(null) }
    var requestVoicePermission by remember { mutableStateOf(false) }

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

            // Import the checklist using ViewModel
            viewModel.importChecklist(uri)
        }
    }

    // Handle toast events
    toastEvent?.let {
        JarvisToast(
            message = it.message,
            onDismiss = { viewModel.clearToastEvent() }
        )
    }

    // Permission request handling
    if (requestVoicePermission) {
        RequestAudioPermission(
            onPermissionGranted = {
                viewModel.setVoiceControlEnabled(true)
                requestVoicePermission = false
            },
            onPermissionDenied = {
                viewModel.setVoiceControlEnabled(false)
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
                    viewModel.deleteChecklist(checklist.id)
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
        },
        containerColor = JarvisTheme.colorScheme.background,
        contentColor = JarvisTheme.colorScheme.onBackground
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
                            viewModel.setVoiceControlEnabled(true)
                        } else {
                            // Request permission if not granted
                            requestVoicePermission = true
                        }
                    } else {
                        viewModel.setVoiceControlEnabled(false)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Text-to-Speech Setting
            SettingSwitchItem(
                title = "Text-to-Speech",
                description = "Read checklist items aloud as you navigate",
                icon = if (useTTS) Icons.Default.Mic else Icons.Default.MicOff,
                isChecked = useTTS,
                onCheckedChange = { isChecked ->
                    viewModel.setTtsEnabled(isChecked)
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
                    viewModel.setThemeMode(newThemeMode)
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
                    viewModel.startVoiceTraining()
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
        colors = CardDefaults.cardColors(
            containerColor = JarvisTheme.colorScheme.surface,
            contentColor = JarvisTheme.colorScheme.onSurface
        ),
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
                    fontWeight = FontWeight.SemiBold,
                    color = JarvisTheme.colorScheme.onSurface
                )

                if (checklistInfoData.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = checklistInfoData.description,
                        style = JarvisTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = JarvisTheme.colorScheme.onSurface
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

