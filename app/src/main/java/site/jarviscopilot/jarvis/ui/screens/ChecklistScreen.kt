package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChecklistScreen(
    checklistName: String,
    onNavigateHome: () -> Unit
) {
    val currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val localTimeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val utcTimeFormat = remember { SimpleDateFormat("HH:mm:ss 'UTC'", Locale.getDefault()) }

    // Update the time every second
    LaunchedEffect(key1 = true) {
        while(true) {
            currentTime.longValue = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

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

    Scaffold(
        topBar = {
            Column {
                // Top Ribbon - First Level (Flight plan and times)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Flight Plan: N/A",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        Text(
                            text = "Local: ${localTimeFormat.format(Date(currentTime.longValue))}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )

                        Text(
                            text = utcTimeFormat.format(Date(currentTime.longValue)),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

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
                                modifier = Modifier.size(28.dp)
                            )
                            Text("Home", fontSize = 12.sp)
                        }
                    }

                    // Check button
                    Button(
                        onClick = {
                            // Simulate marking the current item as complete
                            val nextItemToComplete = checklistItems.indices.firstOrNull { it !in completedItems }
                            if (nextItemToComplete != null) {
                                completedItems.add(nextItemToComplete)
                            }
                        },
                        modifier = Modifier.size(width = 100.dp, height = 60.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                modifier = Modifier.size(28.dp)
                            )
                            Text("Check", fontSize = 14.sp)
                        }
                    }

                    // Skip button (greyed out if not permitted)
                    OutlinedButton(
                        onClick = { /* Skip logic would go here */ },
                        modifier = Modifier.size(width = 100.dp, height = 60.dp),
                        enabled = false // greyed out as per requirements
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Skip", fontSize = 14.sp)
                        }
                    }

                    // Mic button
                    IconButton(onClick = { /* Mic logic would go here */ }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Microphone",
                                modifier = Modifier.size(28.dp)
                            )
                            Text("Listen", fontSize = 12.sp)
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

