package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun ChecklistBar(
    onNavigateHome: () -> Unit,
    onCheckItem: () -> Unit,
    onSkipItem: () -> Unit,
    onSearchItem: () -> Unit,
    onSearchRequiredItem: () -> Unit,    // Added parameter for searching required items
    onToggleMic: () -> Unit,
    onEmergency: () -> Unit,
    isMicActive: Boolean,
    isActiveItemEnabled: Boolean
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = JarvisTheme.colorScheme.primaryContainer,
        contentColor = JarvisTheme.colorScheme.onPrimaryContainer,
        contentPadding = PaddingValues(vertical = 6.dp)  // Added reduced padding
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home button
            JarvisIconButton(
                icon = Icons.Default.Home,
                onClick = onNavigateHome,
                iconTint = JarvisTheme.colorScheme.onPrimary,
                containerColor = JarvisTheme.colorScheme.primary
            )

            // Check button - mark current item as complete
            JarvisIconButton(
                icon = Icons.Default.Check,
                onClick = onCheckItem,
                iconTint = JarvisTheme.colorScheme.onPrimary,
                containerColor = JarvisTheme.colorScheme.primary
            )

            // Skip button - skip current item if allowed
            JarvisIconButton(
                icon = Icons.Default.SkipNext,
                onClick = onSkipItem,
                enabled = isActiveItemEnabled,
                iconTint = JarvisTheme.colorScheme.onPrimary,
                containerColor = JarvisTheme.colorScheme.primary
            )

            // Search button - find first unchecked item (click) or required item (long click)
            JarvisIconButton(
                icon = Icons.Default.Search,
                onClick = onSearchItem,
                onLongClick = onSearchRequiredItem,
                iconTint = JarvisTheme.colorScheme.onPrimary,
                containerColor = JarvisTheme.colorScheme.primary
            )

            // Mic button - toggles listening for voice commands
            JarvisIconButton(
                icon = Icons.Default.Mic,
                onClick = onToggleMic,
                iconTint = if (isMicActive)
                    JarvisTheme.colorScheme.tertiary
                else
                    JarvisTheme.colorScheme.onPrimary,
                containerColor = JarvisTheme.colorScheme.primary
            )

            // Emergency button - displays emergency checklists
            JarvisIconButton(
                icon = Icons.Default.Warning,
                onClick = onEmergency,
                iconTint = JarvisTheme.colorScheme.onEmergencyContainer,
                containerColor = JarvisTheme.colorScheme.emergencyContainer
            )
        }
    }
}
