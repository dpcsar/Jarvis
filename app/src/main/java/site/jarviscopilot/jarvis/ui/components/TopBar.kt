package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors

/**
 * Top bar component with two-level ribbon layout
 * Includes status bar padding for edge-to-edge design
 */
@Composable
fun TopBar(
    localTime: String,
    utcTime: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current
    
    Column(modifier = modifier.fillMaxWidth()) {
        // First level ribbon with status bar insets handling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(aviationColors.avDarkBlue)
                .statusBarsPadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu button
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = aviationColors.avTextWhite
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Time displays aligned to end
            Column(
                modifier = Modifier.padding(bottom = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Local: $localTime",
                        color = aviationColors.avTextWhite,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "UTC: $utcTime",
                        color = aviationColors.avTextWhite,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    JarvisTheme {
        TopBar(
            localTime = "12:34:56",
            utcTime = "17:34:56",
            onMenuClick = {}
        )
    }
}