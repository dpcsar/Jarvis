package site.jarviscopilot.jarvis.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

// A customized Toast-like component that uses the Jarvis theme colors
@Composable
fun JarvisToast(
    message: String,
    duration: Long = 2000,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(if (isVisible) 1f else 0f)

    LaunchedEffect(Unit) {
        delay(duration - 300) // Account for fade-out animation time
        isVisible = false
        delay(300)
        onDismiss()
    }

    Popup(alignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .alpha(alpha)
                .background(
                    color = JarvisTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message,
                color = JarvisTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

