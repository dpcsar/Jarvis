package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import site.jarviscopilot.jarvis.ui.components.BottomBar
import site.jarviscopilot.jarvis.ui.components.ChecklistItemComponent
import site.jarviscopilot.jarvis.ui.components.ChecklistSelector
import site.jarviscopilot.jarvis.ui.components.SectionHeader
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel
import androidx.core.graphics.toColorInt

@Composable
fun ChecklistDetailScreen(
    listIndex: Int,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChecklistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Set the selected list when the screen is first displayed
    LaunchedEffect(key1 = listIndex) {
        viewModel.selectList(listIndex)
    }
    
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
            Column {
                // List selector bar at the bottom
                uiState.checklist?.let { checklist ->
                    ChecklistSelector(
                        lists = checklist.children,
                        selectedIndex = uiState.selectedListIndex,
                        onListSelected = { viewModel.selectList(it) }
                    )
                }
                
                // Control buttons
                BottomBar(
                    onHomeClick = onNavigateUp,
                    onCheckClick = { viewModel.checkCurrentItem() },
                    onSkipClick = { viewModel.skipCurrentItem() },
                    onMicClick = { /* Implement voice feature later */ },
                    onRepeatClick = { /* Implement repeat feature later */ },
                    canSkip = true, // Could be based on mandatory status later
                    isListening = false // Will be true when listening for voice
                )
            }
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
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                val currentList = uiState.checklist?.children?.getOrNull(uiState.selectedListIndex)
                if (currentList != null) {
                    val listState = rememberLazyListState()
                    val currentSectionIndex = uiState.selectedSectionIndex
                    val currentItemIndex = uiState.selectedItemIndex
                    
                    // Calculate the overall index for scrolling
                    val targetIndex by remember(currentSectionIndex, currentItemIndex) {
                        derivedStateOf {
                            var index = 0
                            for (i in 0 until currentSectionIndex) {
                                index += 1 // Section header
                                index += currentList.children[i].children.size
                                index += 1 // Spacer after section
                            }
                            index += 1 // Current section header
                            index += currentItemIndex
                            
                            index
                        }
                    }
                    
                    // Scroll to the selected item
                    LaunchedEffect(targetIndex) {
                        listState.animateScrollToItem(targetIndex.coerceAtLeast(0))
                    }
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        currentList.children.forEachIndexed { sectionIndex, section ->
                            // Section header
                            item {
                                SectionHeader(
                                    title = section.name,
                                    backgroundColor = Color(section.backgroundColor.toColorInt()),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            // Section items
                            items(section.children.size) { itemIndex ->
                                val item = section.children[itemIndex]
                                val isSelected = sectionIndex == currentSectionIndex && itemIndex == currentItemIndex
                                
                                if (item.visible) {
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
                            
                            // Spacer after section
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
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