package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController
) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                Notification(
                    id = "1",
                    title = "Weather Alert",
                    message = "Heavy rain expected in your area today",
                    timestamp = LocalDateTime.now().minusHours(2)
                ),
                Notification(
                    id = "2",
                    title = "Flight Status",
                    message = "Your flight UA123 is delayed by 30 minutes",
                    timestamp = LocalDateTime.now().minusHours(5)
                ),
                Notification(
                    id = "3",
                    title = "System Update",
                    message = "Jarvis has been updated to the latest version",
                    timestamp = LocalDateTime.now().minusDays(1)
                ),
                Notification(
                    id = "4", 
                    title = "Reminder",
                    message = "Meeting with flight crew at 15:00",
                    timestamp = LocalDateTime.now().minusDays(2),
                    isRead = true
                )
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopBar(
                title = "Notifications",
                onBackPressed = { navController.popBackStack() },
                showBackButton = true
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyNotifications(paddingValues)
        } else {
            NotificationsList(
                notifications = notifications,
                onNotificationClick = { notification ->
                    // Mark as read
                    notifications = notifications.map {
                        if (it.id == notification.id) it.copy(isRead = true) else it
                    }
                },
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
fun EmptyNotifications(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "No notifications yet",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "When you receive notifications, they will appear here",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsList(
    notifications: List<Notification>,
    onNotificationClick: (Notification) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notifications) { notification ->
            NotificationItem(
                notification = notification,
                onClick = { onNotificationClick(notification) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = formatter.format(notification.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    JarvisTheme {
        NotificationsScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyNotificationsPreview() {
    JarvisTheme {
        EmptyNotifications(PaddingValues(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationItemPreview() {
    JarvisTheme {
        NotificationItem(
            notification = Notification(
                id = "1",
                title = "Weather Alert",
                message = "Heavy rain expected in your area today",
                timestamp = LocalDateTime.now().minusHours(2)
            ),
            onClick = {}
        )
    }
}