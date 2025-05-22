package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.jarviscopilot.jarvis.data.repository.ChecklistInfo
import site.jarviscopilot.jarvis.data.repository.ChecklistRepository
import site.jarviscopilot.jarvis.ui.components.TopRibbon

@Composable
fun MainScreen(
    onChecklistSelected: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ChecklistRepository(context) }
    var checklistInfoList by remember { mutableStateOf<List<ChecklistInfo>>(emptyList()) }

    // Load checklists using the repository
    LaunchedEffect(Unit) {
        checklistInfoList = repository.loadChecklistInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Using the TopRibbon component
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

        // List of checklists from repository
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(checklistInfoList) { checklistInfo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            onChecklistSelected(checklistInfo.filename)
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = checklistInfo.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = checklistInfo.description,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
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
    // Create a preview version that uses mock data instead of the real repository
    val mockChecklistInfoList = listOf(
        ChecklistInfo("Cessna 172", "Checklist for Cessna 172", "cl_cessna172.json"),
        ChecklistInfo("Piper Cherokee", "Checklist for Piper Cherokee", "cl_piper_cherokee.json"),
        ChecklistInfo("Vans RV6", "Checklist for Vans RV6", "cl_vans_rv6.json")
    )

    CompositionLocalProvider(
        LocalContext provides LocalContext.current
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Using the TopRibbon component
            TopRibbon(
                onSettingsClick = {}
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

            // List of checklists with mock data
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(mockChecklistInfoList) { checklistInfo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = checklistInfo.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = checklistInfo.description,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preview with dark mode
@Preview(
    showBackground = true,
    apiLevel = 35,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MainScreenPreviewDark() {
    // Create a preview version that uses mock data instead of the real repository
    val mockChecklistInfoList = listOf(
        ChecklistInfo("Cessna 172", "Checklist for Cessna 172", "cl_cessna172.json"),
        ChecklistInfo("Piper Cherokee", "Checklist for Piper Cherokee", "cl_piper_cherokee.json"),
        ChecklistInfo("Vans RV6", "Checklist for Vans RV6", "cl_vans_rv6.json")
    )

    CompositionLocalProvider(
        LocalContext provides LocalContext.current
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Using the TopRibbon component
            TopRibbon(
                onSettingsClick = {}
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

            // List of checklists with mock data
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(mockChecklistInfoList) { checklistInfo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = checklistInfo.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = checklistInfo.description,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
