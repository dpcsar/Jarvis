package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ChecklistBottomRibbon(
    onNavigateHome: () -> Unit,
    onCheckItem: () -> Unit,
    onSkipItem: () -> Unit,
    onSearchItem: () -> Unit,
    onToggleMic: () -> Unit,
    onEmergency: () -> Unit,
    isMicActive: Boolean,
    isActiveItemEnabled: Boolean
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home button
            JarvisIconButton(
                icon = Icons.Default.Home,
                onClick = onNavigateHome
            )

            // Check button - mark current item as complete
            JarvisIconButton(
                icon = Icons.Default.Check,
                onClick = onCheckItem
            )

            // Skip button - skip current item if allowed
            JarvisIconButton(
                icon = Icons.Default.SkipNext,
                onClick = onSkipItem,
                enabled = isActiveItemEnabled
            )

            // Search button - find first skipped item
            JarvisIconButton(
                icon = Icons.Default.Search,
                onClick = onSearchItem
            )

            // Mic button - toggles listening for voice commands
            JarvisIconButton(
                icon = Icons.Default.Mic,
                onClick = onToggleMic,
                iconTint = if (isMicActive)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.onPrimary
            )

            // Emergency button - displays emergency checklists
            JarvisIconButton(
                icon = Icons.Default.Warning,
                onClick = onEmergency,
                iconTint = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        }
    }
}
