package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.jarviscopilot.jarvis.ui.components.TopRibbon

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

    // Function to find the first unchecked item
    val findFirstUnchecked = {
        checklistItems.indices.firstOrNull { it !in completedItems }
    }

    Scaffold(
        topBar = {
            Column {
                // Use the reusable TopRibbon component
                TopRibbon()

                // Top Ribbon - Second Level (Current phase of flight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = checklistName,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        },
        bottomBar = {
            // Bottom Ribbon with action buttons
            BottomAppBar(
                modifier = Modifier.height(80.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home button
                    IconButton(onClick = onNavigateHome) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Home", fontSize = 12.sp)
                        }
                    }

                    // Check button
                    Button(
                        onClick = {
                            // Mark the next unchecked item as complete
                            val nextItemToComplete = findFirstUnchecked()
                            if (nextItemToComplete != null) {
                                completedItems.add(nextItemToComplete)
                            }
                        },
                        modifier = Modifier.size(width = 80.dp, height = 60.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Check", fontSize = 12.sp)
                        }
                    }

                    // Skip button
                    OutlinedButton(
                        onClick = { /* Skip the current task */ },
                        modifier = Modifier.size(width = 80.dp, height = 60.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Skip", fontSize = 12.sp)
                        }
                    }

                    // Mic button
                    IconButton(onClick = { /* Enable voice listening */ }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Listen",
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Listen", fontSize = 12.sp)
                        }
                    }

                    // Search button - find first unchecked task
                    IconButton(onClick = { /* Find first unchecked task */ }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Find",
                                modifier = Modifier.size(24.dp)
                            )
                            Text("Find", fontSize = 12.sp)
                        }
                    }

                    // Emergency button
                    IconButton(
                        onClick = { /* Navigate to emergency checklists */ },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text("Emergency", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Checklist content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(checklistItems.size) { index ->
                val isCompleted = index in completedItems

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { checked ->
                            if (checked) {
                                completedItems.add(index)
                            } else {
                                completedItems.remove(index)
                            }
                        }
                    )

                    Text(
                        text = checklistItems[index],
                        fontSize = 16.sp,
                        fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (index < checklistItems.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
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
fun ChecklistScreenPreviewDark() {
    MaterialTheme {
        ChecklistScreen(
            checklistName = "Pre-Flight Checklist",
            onNavigateHome = {}
        )
    }
}

