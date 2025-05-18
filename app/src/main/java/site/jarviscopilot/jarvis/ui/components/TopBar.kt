package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.AvDarkBlue
import site.jarviscopilot.jarvis.ui.theme.AvDarkGrey
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun TopBar(
    localTime: String,
    utcTime: String,
    currentPhase: String,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // First level ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AvDarkBlue)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Local: $localTime",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "UTC: $utcTime",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        // Second level ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AvDarkGrey)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Phase: $currentPhase",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview
@Composable
fun TopBarPreview() {
    JarvisTheme {
        TopBar(
            localTime = "12:34:56",
            utcTime = "17:34:56",
            currentPhase = "PreFlight",
            onMenuClick = {}
        )
    }
}