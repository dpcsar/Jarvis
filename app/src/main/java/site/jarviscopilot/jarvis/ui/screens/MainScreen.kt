package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    onChecklistSelected: (String) -> Unit,
    onSettingsClick: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Ribbon - First Level
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

        // Top Ribbon - Second Level
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Checklist Title
        Text(
            text = "Available Checklists",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Sample checklist data - this would be replaced with actual data from your JSON file
        val sampleChecklists = remember {
            listOf(
                "Pre-Flight",
                "Before Start",
                "After Start",
                "Taxiing",
                "Before Takeoff",
                "After Takeoff",
                "Cruise",
                "Descent",
                "Before Landing",
                "After Landing",
                "Shutdown"
            )
        }

        // List of checklists
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(sampleChecklists) { checklist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onChecklistSelected(checklist) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = checklist,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

