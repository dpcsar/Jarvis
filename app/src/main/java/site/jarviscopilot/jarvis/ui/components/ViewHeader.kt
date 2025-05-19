package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors

@Composable
fun ViewHeader(
    title: String,
    sectionType: String? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current

    // Determine background color based on section type if provided
    val headerBackgroundColor = backgroundColor ?: when(sectionType?.lowercase()) {
        "emergency" -> aviationColors.avRed
        "reference" -> aviationColors.avBlue
        else -> aviationColors.avGreen
    }

    // Determine text color based on background color
    val headerTextColor = textColor ?: when(headerBackgroundColor) {
        aviationColors.avGreen -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(headerBackgroundColor)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = headerTextColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(apiLevel = 35)
@Composable
fun SectionHeaderPreview() {
    JarvisTheme {
        ViewHeader(title = "Pilot currency and proficiency")
    }
}

