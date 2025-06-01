package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.util.TimeUtil

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    flightPlan: String? = null
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
            .background(JarvisTheme.colorScheme.primaryContainer)
    ) {
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
                    style = JarvisTheme.typography.bodyLarge,
                    color = JarvisTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Local: $localTime",
                        style = JarvisTheme.typography.bodyMedium,
                        color = JarvisTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "UTC: $utcTime",
                        style = JarvisTheme.typography.bodyMedium,
                        color = JarvisTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        HorizontalDivider(
            color = JarvisTheme.colorScheme.outline
        )
    }
}

