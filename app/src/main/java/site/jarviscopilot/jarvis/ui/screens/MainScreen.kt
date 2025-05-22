package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.jarviscopilot.jarvis.ui.components.TopRibbon

@Composable
fun MainScreen(
    onChecklistSelected: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Using the TopRibbon component instead of the hardcoded implementation
        TopRibbon(
            onSettingsClick = onSettingsClick
        )

        // Checklist Title
        Text(
            text = "Available Checklists",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Sample checklist data - this would be replaced with actual data from your JSON file
        val sampleChecklists = remember {
            listOf(
                "Pre-Flight",
                "Before Start",
                "After Start",
                "Taxiing",
                "Before Takeoff",
                "After Takeoff",
                "Cruise",
                "Descent",
                "Before Landing",
                "After Landing",
                "Shutdown"
            )
        }

        // List of checklists
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(sampleChecklists) { checklist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onChecklistSelected(checklist) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = checklist,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

// Preview with lite mode
@Preview(
    showBackground = true,
    apiLevel = 35,
)
@Composable
fun MainScreenPreview() {
    MainScreen(
        onChecklistSelected = {},
        onSettingsClick = {}
    )
}

// Preview with dark mode
@Preview(
    showBackground = true,
    apiLevel = 35,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MainScreenPreviewDark() {
    MainScreen(
        onChecklistSelected = {},
        onSettingsClick = {}
    )
}
