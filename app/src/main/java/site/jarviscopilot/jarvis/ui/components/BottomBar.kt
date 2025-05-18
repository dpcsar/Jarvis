package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.AvBlack
import site.jarviscopilot.jarvis.ui.theme.AvGreen
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onCheckClick: () -> Unit,
    onSkipClick: () -> Unit,
    onMicClick: () -> Unit,
    onRepeatClick: () -> Unit,
    canSkip: Boolean = true,
    isListening: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AvBlack)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home button
        IconButton(onClick = onHomeClick) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Check button (larger)
        IconButton(onClick = onCheckClick) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Check",
                tint = AvGreen,
                modifier = Modifier.size(40.dp)
            )
        }
        
        // Skip button (larger, conditionally enabled)
        IconButton(
            onClick = onSkipClick,
            enabled = canSkip
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = "Skip",
                tint = if (canSkip) Color.White else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
        }
        
        // Mic button (changes color when listening)
        IconButton(onClick = onMicClick) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Mic",
                tint = if (isListening) AvGreen else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Repeat button
        IconButton(onClick = onRepeatClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Repeat",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    JarvisTheme {
        BottomBar(
            onHomeClick = {},
            onCheckClick = {},
            onSkipClick = {},
            onMicClick = {},
            onRepeatClick = {},
            canSkip = true,
            isListening = false
        )
    }
}