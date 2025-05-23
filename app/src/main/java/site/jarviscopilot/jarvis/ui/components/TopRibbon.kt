package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import site.jarviscopilot.jarvis.util.TimeUtil

@Composable
fun TopRibbon(
    modifier: Modifier = Modifier,
    flightPlan: String? = null,
    onSettingsClick: () -> Unit
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
                Text(
                    text = flightPlan ?: "No Flight Plan",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Local: $localTime",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "UTC: $utcTime",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        HorizontalDivider()

        // Second level: Settings button and current phase
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side content can go here
            Text("", modifier = Modifier.weight(1f))

            // Settings button moved to the right
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

// Preview with lite mode
@Preview(
    showBackground = true,
    apiLevel = 35
)
@Composable
fun TopRibbonPreview() {
    MaterialTheme {
        TopRibbon(
            flightPlan = "KDEN - KLAX",
            onSettingsClick = {}
        )
    }
}

// Preview with dark mode
@Preview(
    showBackground = true,
    apiLevel = 35,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TopRibbonPreviewDark() {
    MaterialTheme {
        TopRibbon(
            flightPlan = "KDEN - KLAX",
            onSettingsClick = {}
        )
    }
}

