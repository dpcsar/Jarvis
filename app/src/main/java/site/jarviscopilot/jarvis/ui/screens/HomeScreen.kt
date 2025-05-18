package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import site.jarviscopilot.jarvis.model.Checklist
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel

@Composable
fun HomeScreen(
    onChecklistSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChecklistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val aviationColors = LocalAviationColors.current
    
    Scaffold(
        topBar = {
            TopBar(
                localTime = uiState.currentLocalTime,
                utcTime = uiState.currentUtcTime,
                currentPhase = uiState.currentPhase,
                onMenuClick = { /* Open menu - implement later */ }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = aviationColors.avRed,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                uiState.checklist?.let { checklist ->
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Text(
                            text = "Available Checklists",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = aviationColors.textOnSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(aviationColors.headerBackground)
                                .padding(16.dp)
                        )
                        
                        Text(
                            text = "Select a checklist to view its details:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = aviationColors.textOnBackground,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        
                        // List of checklists
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            itemsIndexed(checklist.children) { index, list ->
                                ChecklistListItem(
                                    list = list,
                                    onClick = { onChecklistSelected(index) },
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistListItem(
    list: ChecklistList,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current
    val isEmergency = list.name.contains("Emergency", ignoreCase = true)
    val backgroundColor = if (isEmergency) aviationColors.avRed.copy(alpha = 0.8f) else aviationColors.avBlue
    val textColor = aviationColors.textOnSurface
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = list.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    JarvisTheme {
        val mockChecklist = Checklist(
            name = "C172S Checklist",
            description = "Standard procedures for Cessna 172S",
            children = listOf(
                ChecklistList(
                    name = "Normal Procedures",
                    type = "list"
                ),
                ChecklistList(
                    name = "Emergency Procedures",
                    type = "list"
                ),
                ChecklistList(
                    name = "Performance Data",
                    type = "list"
                )
            )
        )
        
        HomeScreenPreviewContent(mockChecklist)
    }
}

@Composable
private fun HomeScreenPreviewContent(checklist: Checklist) {
    val aviationColors = LocalAviationColors.current
    
    Scaffold(
        topBar = {
            TopBar(
                localTime = "12:34:56",
                utcTime = "16:34:56",
                currentPhase = "PreFlight",
                onMenuClick = { }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    text = "Available Checklists",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = aviationColors.textOnSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(aviationColors.headerBackground)
                        .padding(16.dp)
                )
                
                Text(
                    text = "Select a checklist to view its details:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = aviationColors.textOnBackground,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // List of checklists
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    itemsIndexed(checklist.children) { index, list ->
                        ChecklistListItem(
                            list = list,
                            onClick = { },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}