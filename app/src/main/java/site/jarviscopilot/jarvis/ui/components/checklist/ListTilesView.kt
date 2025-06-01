package site.jarviscopilot.jarvis.ui.components.checklist

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.model.ChecklistListData
import site.jarviscopilot.jarvis.ui.components.ListTiles

/**
 * A composable that displays a grid of checklist tiles.
 */
@Composable
fun ListTilesView(
    lists: List<ChecklistListData>,
    sectionType: String,
    onTileClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        items(lists.indices.toList()) { listIndex ->
            val list = lists[listIndex]
            ListTiles(
                checklistListData = list,
                onTileClick = { onTileClick(listIndex) },
                category = sectionType
            )
        }
    }
}
