package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.components.ChecklistItemType
import site.jarviscopilot.jarvis.ui.components.JarvisChecklistItem
import site.jarviscopilot.jarvis.ui.components.JarvisConfirmationDialog
import site.jarviscopilot.jarvis.ui.components.JarvisIconButton
import site.jarviscopilot.jarvis.ui.components.TopRibbon
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun ChecklistScreen(
    checklistName: String,
    onNavigateHome: () -> Unit
) {
    // Sample checklist items - would be replaced with data from your JSON file
    val checklistItems = remember {
        listOf(
            "Check fuel levels",
            "Verify oil pressure",
            "Test magnetos",
            "Check flight controls",
            "Verify radio communications",
            "Set altimeter",
            "Check transponder"
        )
    }

    // Track which items are completed
    val completedItems = remember { mutableStateListOf<Int>() }

    // Track the currently active item
    val activeItemIndex = remember { mutableIntStateOf(0) }

    // Track whether the mic is active
    val isMicActive = remember { mutableStateOf(false) }

    // Track if a dialog is showing
    val showDialog = remember { mutableStateOf(false) }

    // Function to find the first unchecked item
    val findFirstUnchecked = {
        checklistItems.indices.firstOrNull { it !in completedItems }
    }

    Scaffold(
        topBar = {
            Column {
                TopRibbon()
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home button
                    JarvisIconButton(
                        text = "Home",
                        icon = Icons.Default.Home,
                        onClick = onNavigateHome
                    )

                    // Check button - mark current item as complete
                    JarvisIconButton(
                        text = "Check",
                        icon = Icons.Default.Check,
                        onClick = {
                            if (activeItemIndex.intValue < checklistItems.size &&
                                activeItemIndex.intValue !in completedItems
                            ) {
                                completedItems.add(activeItemIndex.intValue)
                                // Move to next item if available
                                findFirstUnchecked()?.let {
                                    activeItemIndex.intValue = it
                                }
                            }
                        }
                    )

                    // Skip button - skip current item if allowed
                    JarvisIconButton(
                        text = "Skip",
                        icon = Icons.Default.SkipNext,
                        onClick = {
                            showDialog.value = true
                        },
                        enabled = activeItemIndex.intValue < checklistItems.size
                    )

                    // Mic button - toggles listening for voice commands
                    JarvisIconButton(
                        text = "Listen",
                        icon = Icons.Default.Mic,
                        onClick = {
                            isMicActive.value = !isMicActive.value
                        },
                        iconTint = if (isMicActive.value)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onPrimary
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
        ) {
            // Checklist title
            Text(
                text = checklistName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Checklist items
            LazyColumn {
                itemsIndexed(checklistItems) { index, item ->
                    JarvisChecklistItem(
                        text = item,
                        isCompleted = index in completedItems,
                        type = if (index % 3 == 0) ChecklistItemType.WARNING else ChecklistItemType.NORMAL,
                        isActive = index == activeItemIndex.intValue,
                        onItemClick = {
                            activeItemIndex.intValue = index
                        }
                    )
                }
            }
        }

        // Skip confirmation dialog
        if (showDialog.value) {
            JarvisConfirmationDialog(
                title = "Skip Item",
                message = "Are you sure you want to skip this item?",
                onConfirmClick = {
                    // Skip the current item and move to next
                    val nextUncheckedItem =
                        checklistItems.indices.firstOrNull { it > activeItemIndex.intValue && it !in completedItems }
                            ?: activeItemIndex.intValue
                    activeItemIndex.intValue = nextUncheckedItem
                    showDialog.value = false
                },
                onDismissClick = {
                    showDialog.value = false
                },
                onDismissRequest = {
                    showDialog.value = false
                },
                confirmText = "Skip",
                dismissText = "Cancel"
            )
        }
    }
}

@Preview( name = "Light Mode", apiLevel = 35, showBackground = true )
@Composable
fun ChecklistScreenLightPreview() {
    JarvisTheme {
        ChecklistScreen(
            checklistName = "Pre-Flight Checklist",
            onNavigateHome = {}
        )
    }
}

@Preview( name = "Dark Mode", apiLevel = 35, showBackground = true )
@Composable
fun ChecklistScreenDarkPreview() {
    JarvisTheme(darkTheme = true) {
        ChecklistScreen(
            checklistName = "Pre-Flight Checklist",
            onNavigateHome = {}
        )
    }
}
