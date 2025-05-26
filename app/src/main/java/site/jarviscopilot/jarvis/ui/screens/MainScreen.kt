package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.ChecklistInfo
import site.jarviscopilot.jarvis.data.ChecklistRepository
import site.jarviscopilot.jarvis.ui.components.JarvisButton
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun MainScreen(
    onChecklistSelected: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ChecklistRepository(context) }
    var checklistInfoList by remember { mutableStateOf<List<ChecklistInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        checklistInfoList = repository.loadAllChecklists()
        isLoading = false
    }

    Scaffold(
        topBar = {
            Column {
                TopBar()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = JarvisTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(JarvisTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jarvis Aviation Assistant",
                style = JarvisTheme.typography.headlineMedium,
                color = JarvisTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Select a Checklist",
                style = JarvisTheme.typography.titleLarge,
                color = JarvisTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                // Show loading indicator while data is being loaded
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = JarvisTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Loading checklists...",
                        style = JarvisTheme.typography.bodyLarge,
                        color = JarvisTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (checklistInfoList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No checklists found",
                        style = JarvisTheme.typography.bodyLarge,
                        color = JarvisTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    JarvisButton(
                        onClick = onSettingsClick
                    ) {
                        Text("Go to Settings")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(checklistInfoList) { checklistInfo ->
                        ChecklistCard(
                            checklistInfo = checklistInfo,
                            onSelected = { onChecklistSelected(checklistInfo.filename) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistCard(
    checklistInfo: ChecklistInfo,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = JarvisTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = checklistInfo.name,
                style = JarvisTheme.typography.titleMedium,
                color = JarvisTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = checklistInfo.description,
                style = JarvisTheme.typography.bodyMedium,
                color = JarvisTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview( name = "Light Mode", apiLevel = 35, showBackground = true )
@Composable
fun MainScreenLightPreview() {
    JarvisTheme {
        MainScreen(
            onChecklistSelected = {},
            onSettingsClick = {}
        )
    }
}

@Preview( name = "Dark Mode", apiLevel = 35, showBackground = true )
@Composable
fun MainScreenDarkPreview() {
    JarvisTheme (darkTheme = true) {
        MainScreen(
            onChecklistSelected = {},
            onSettingsClick = {}
        )
    }
}
