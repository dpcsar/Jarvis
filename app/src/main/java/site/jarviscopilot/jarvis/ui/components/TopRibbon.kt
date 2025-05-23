package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun TopRibbon(
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
            .background(MaterialTheme.colorScheme.primaryContainer)
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
    }
}

// Preview composable that shows the UI in both light and dark modes
@Preview(
    name = "Light Mode",
    apiLevel = 35,
    showBackground = true
)
@Composable
fun TopRibbonPreview() {
    JarvisTheme(darkTheme = false) {
        TopRibbon(
            flightPlan = "KDEN - KLAX"
        )
    }
}

@Preview(
    name = "Dark Mode",
    apiLevel = 35,
    showBackground = true
)
@Composable
fun TopRibbonPreviewDark() {
    JarvisTheme(darkTheme = true) {
        TopRibbon(
            flightPlan = "KDEN - KLAX"
        )
    }
}

