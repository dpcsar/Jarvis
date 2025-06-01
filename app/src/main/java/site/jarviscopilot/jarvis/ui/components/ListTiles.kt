package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.model.ChecklistListData
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

/**
 * A tile representation of a checklist list for the tile view.
 */
@Composable
fun ListTiles(
    modifier: Modifier = Modifier,
    checklistListData: ChecklistListData,
    onTileClick: () -> Unit,
    category: String = ""
) {
    // Determine colors based on category
    val containerColor = when {
        category.equals(
            "emergency",
            ignoreCase = true
        ) -> JarvisTheme.colorScheme.emergencyContainer

        category.equals(
            "reference",
            ignoreCase = true
        ) -> JarvisTheme.colorScheme.referenceContainer

        else -> JarvisTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        category.equals(
            "emergency",
            ignoreCase = true
        ) -> JarvisTheme.colorScheme.onEmergencyContainer

        category.equals(
            "reference",
            ignoreCase = true
        ) -> JarvisTheme.colorScheme.onReferenceContainer

        else -> JarvisTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)  // Square aspect ratio
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onTileClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title of the list
            Text(
                text = checklistListData.listTitle,
                style = JarvisTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Item count
            Text(
                text = "${checklistListData.listItems.size} items",
                style = JarvisTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
