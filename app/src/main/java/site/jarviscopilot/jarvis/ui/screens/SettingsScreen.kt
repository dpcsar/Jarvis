package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import site.jarviscopilot.jarvis.ui.components.SettingsItem
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    var wakeWordEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var dataCollectionEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopBar(
                title = "Settings",
                onBackPressed = { navController.popBackStack() },
                showBackButton = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Voice Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            SettingsItem(
                title = "Wake Word Detection",
                description = "Enable 'Hey Jarvis' wake word detection",
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                trailingContent = {
                    Switch(
                        checked = wakeWordEnabled,
                        onCheckedChange = { wakeWordEnabled = it }
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            SettingsItem(
                title = "Push Notifications",
                description = "Receive notifications for important updates",
                icon = Icons.Default.Notifications,
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            SettingsItem(
                title = "Profile Settings",
                description = "Update your profile information",
                icon = Icons.Default.Person,
                onClick = { navController.navigate("profile") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Privacy",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            SettingsItem(
                title = "Data Collection",
                description = "Allow anonymous data collection to improve service",
                icon = Icons.Default.Security,
                trailingContent = {
                    Switch(
                        checked = dataCollectionEnabled,
                        onCheckedChange = { dataCollectionEnabled = it }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    JarvisTheme {
        SettingsScreen(
            navController = rememberNavController()
        )
    }
}

