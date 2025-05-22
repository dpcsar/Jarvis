package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import site.jarviscopilot.jarvis.util.TimeUtil

@Composable
fun TopRibbon(
    flightPlan: String? = null,
    currentPhase: String? = null,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localTime by remember { mutableStateOf(TimeUtil.getCurrentLocalTime()) }
    var utcTime by remember { mutableStateOf(TimeUtil.getCurrentUtcTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            localTime = TimeUtil.getCurrentLocalTime()
            utcTime = TimeUtil.getCurrentUtcTime()
            delay(1000) // Update every second
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        // First level: Flight plan, local time, UTC time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* Menu action */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                Text(
                    text = flightPlan ?: "No Flight Plan",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Local: $localTime",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = utcTime,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Divider()

        // Second level: Settings button and current phase
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPhase != null) {
                Text(
                    text = "Current Phase: $currentPhase",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}
