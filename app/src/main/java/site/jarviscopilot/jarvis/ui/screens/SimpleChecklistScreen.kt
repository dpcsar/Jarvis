package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.util.Log
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.ui.components.BottomBar
import site.jarviscopilot.jarvis.ui.components.ChecklistItemComponent
import site.jarviscopilot.jarvis.ui.components.SectionHeader
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.AvBlue
import site.jarviscopilot.jarvis.ui.theme.AvRed
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel

@Composable
fun SimpleChecklistScreen(
    viewModel: ChecklistViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showListSelector by remember { mutableStateOf(true) }
    
    // Reset to list selector when checklist data changes
    LaunchedEffect(key1 = uiState.checklist) {
        if (uiState.checklist != null) {
            Log.d("SimpleChecklistScreen", "Checklist loaded, showing list selector")
            showListSelector = true
        }
    }
    
    Log.d("SimpleChecklistScreen", "UI State: isLoading=${uiState.isLoading}, " +
            "hasChecklist=${uiState.checklist != null}, " +
            "error=${uiState.error}, " +
            "showListSelector=$showListSelector")
    
    Scaffold(
        topBar = {
            TopBar(
                localTime = uiState.currentLocalTime,
                utcTime = uiState.currentUtcTime,
                currentPhase = uiState.currentPhase,
                onMenuClick = { /* Open menu - implement later */ }
            )
        },
        bottomBar = {
            if (!showListSelector) {
                BottomBar(
                    onHomeClick = { showListSelector = true },
                    onCheckClick = { viewModel.checkCurrentItem() },
                    onSkipClick = { viewModel.skipCurrentItem() },
                    onMicClick = { /* Implement voice feature later */ },
                    onRepeatClick = { /* Implement repeat feature later */ },
                    canSkip = true,
                    isListening = false
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = LocalAviationColors.current.avRed,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                uiState.checklist?.let { checklist ->
                    // Debug details of checklist
                    Log.d("SimpleChecklistScreen", "Checklist details: name=${checklist.name}")
                    Log.d("SimpleChecklistScreen", "Number of lists: ${checklist.children.size}")
                    
                    // List all available checklist names for debugging
                    checklist.children.forEachIndexed { index, list ->
                        Log.d("SimpleChecklistScreen", "List $index: ${list.name}, visible=${list.visible}, sections=${list.children.size}")
                    }
                    
                    if (showListSelector) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            val aviationColors = LocalAviationColors.current
                            Text(
                                text = checklist.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = aviationColors.textOnSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(aviationColors.headerBackground)
                                    .padding(16.dp)
                            )
                            
                            if (checklist.description.isNotEmpty()) {
                                Text(
                                    text = checklist.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = aviationColors.textOnBackground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            } else {
                                Text(
                                    text = "Select a flight phase checklist below:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = aviationColors.textOnBackground,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            
                            // List of sections
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(8.dp)
                            ) {
                                itemsIndexed(checklist.children) { index, list ->
                                    ChecklistListItem(
                                        list = list,
                                        onClick = { 
                                            viewModel.selectList(index)
                                            showListSelector = false
                                        },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Show checklist details
                        val currentList = uiState.checklist?.children?.getOrNull(uiState.selectedListIndex)
                        
                        Log.d("SimpleChecklistScreen", "Showing checklist details for list index: ${uiState.selectedListIndex}")
                        
                        if (currentList != null) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "${checklist.name} - ${currentList.name}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = LocalAviationColors.current.textOnSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(LocalAviationColors.current.headerBackground)
                                        .padding(16.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(8.dp)
                                ) {
                                    currentList.children.forEachIndexed { sectionIndex, section ->
                                        item {
                                            SectionHeader(
                                                title = section.name,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        
                                        itemsIndexed(section.children) { itemIndex, item ->
                                            if (item.visible) {
                                                val isSelected = 
                                                    sectionIndex == uiState.selectedSectionIndex && 
                                                    itemIndex == uiState.selectedItemIndex
                                                
                                                ChecklistItemComponent(
                                                    item = item,
                                                    isSelected = isSelected,
                                                    onClick = {
                                                        viewModel.selectSection(sectionIndex)
                                                        viewModel.selectItem(itemIndex)
                                                    },
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                        }
                                        
                                        item {
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black)
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    checklist.children.forEachIndexed { index, list ->
                                        val isSelected = index == uiState.selectedListIndex
                                        val aviationColors = LocalAviationColors.current
                                        val backgroundColor = when {
                                            isSelected -> aviationColors.avBlue
                                            list.name.contains("Emergency", ignoreCase = true) -> aviationColors.avRed
                                            else -> Color.Transparent
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(backgroundColor)
                                                .clickable { 
                                                    viewModel.selectList(index)
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = list.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = aviationColors.textOnSurface,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No checklist selected",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
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