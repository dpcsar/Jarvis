package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import site.jarviscopilot.jarvis.data.model.ChecklistInfoData
import site.jarviscopilot.jarvis.di.AppDependencies
import site.jarviscopilot.jarvis.ui.components.JarvisButton
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.viewmodel.MainViewModel

@Composable
fun MainScreen(
    onChecklistSelected: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit = {}, // Add onHelpClick parameter with default empty implementation
    onResumeChecklist: (String, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val viewModelFactory = AppDependencies.provideMainViewModelFactory(context)
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Refresh checklists whenever screen is resumed.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadChecklists()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Collect consolidated UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopBar()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = JarvisTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(onClick = onHelpClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Help",
                            tint = JarvisTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jarvis Aviation Assistant",
                style = JarvisTheme.typography.headlineMedium,
                color = JarvisTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 24.dp),
                    color = JarvisTheme.colorScheme.primary
                )
            } else {
                // Show error message if exists
                uiState.error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = JarvisTheme.colorScheme.warning,
                        style = JarvisTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (uiState.checklists.isEmpty()) {
                    // Display message and button when no checklists are available
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No checklists available",
                            style = JarvisTheme.typography.titleMedium,
                            color = JarvisTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Please add checklists from the Settings screen",
                            style = JarvisTheme.typography.bodyMedium,
                            color = JarvisTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        JarvisButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("Go to Settings")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.checklists) { checklist ->
                            ChecklistCard(
                                checklist = checklist,
                                canResume = uiState.resumableChecklists.contains(checklist.id),
                                onStart = { onChecklistSelected(checklist.id) },
                                onResume = { onResumeChecklist(checklist.id, true) },
                                onRestart = {
                                    viewModel.clearChecklistStateQuietly(checklist.id)
                                    onChecklistSelected(checklist.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistCard(
    checklist: ChecklistInfoData,
    canResume: Boolean,
    onStart: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = JarvisTheme.colorScheme.surface,
            contentColor = JarvisTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = checklist.name,
                style = JarvisTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = JarvisTheme.colorScheme.onSurface
            )

            if (checklist.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = checklist.description,
                    style = JarvisTheme.typography.bodyMedium,
                    color = JarvisTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (canResume) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JarvisButton(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restart")
                    }

                    JarvisButton(
                        onClick = onResume,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resume")
                    }
                }
            } else {
                JarvisButton(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start")
                }
            }
        }
    }
}
