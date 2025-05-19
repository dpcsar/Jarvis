package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors

@Composable
fun ListTileView(
    lists: List<ChecklistList>,
    sectionType: String,
    onListSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current

    val backgroundColor = when (sectionType.lowercase()) {
        "emergency" -> aviationColors.avRed.copy(alpha = 0.15f)
        "reference" -> aviationColors.avAmber.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when (sectionType.lowercase()) {
        "emergency" -> aviationColors.avRed
        "reference" -> aviationColors.avAmber
        else -> aviationColors.avBlue
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // No ViewHeader here - it should be provided by the parent component (DetailsScreen)

        // Use a grid for the tile-based display of lists
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(lists.withIndex().toList()) { (index, list) ->
                ChecklistTile(
                    list = list,
                    onClick = { onListSelected(index) },
                    borderColor = borderColor,
                    backgroundColor = backgroundColor
                )
            }
        }
    }
}

@Composable
fun ChecklistTile(
    list: ChecklistList,
    onClick: () -> Unit,
    backgroundColor: Color,
    borderColor: Color
) {
    val aviationColors = LocalAviationColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Makes the tiles square
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // List name
            Text(
                text = list.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = aviationColors.textOnBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Item count
            Text(
                text = "${list.items.size} items",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = borderColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemListTileViewPreview() {
    val mockLists = listOf(
        ChecklistList(
            name = "Engine Failure",
            nameAudio = "",
            items = listOf()
        ),
        ChecklistList(
            name = "Engine Fire",
            nameAudio = "",
            items = listOf()
        ),
        ChecklistList(
            name = "Electrical Fire",
            nameAudio = "",
            items = listOf()
        ),
        ChecklistList(
            name = "Loss of Oil Pressure",
            nameAudio = "",
            items = listOf()
        )
    )

    JarvisTheme {
        // Emergency procedures preview
        ListTileView(
            lists = mockLists,
            sectionType = "emergency",
            onListSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }

    JarvisTheme {
        // Reference section preview
        ListTileView(
            lists = listOf(
                ChecklistList(name = "Weight & Balance", nameAudio = "", items = listOf()),
                ChecklistList(name = "V-Speeds", nameAudio = "", items = listOf()),
                ChecklistList(name = "Fuel Consumption", nameAudio = "", items = listOf()),
                ChecklistList(name = "Radio Frequencies", nameAudio = "", items = listOf())
            ),
            sectionType = "reference",
            onListSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
