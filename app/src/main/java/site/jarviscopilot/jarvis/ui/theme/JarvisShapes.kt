package site.jarviscopilot.jarvis.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Shapes used in the Jarvis app.
 */
@Suppress("unused")
@Immutable
class JarvisShapes(
    val small: RoundedCornerShape = RoundedCornerShape(4.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(8.dp),
    val large: RoundedCornerShape = RoundedCornerShape(12.dp),
    val extraLarge: RoundedCornerShape = RoundedCornerShape(16.dp)
)

/**
 * CompositionLocal used to provide [JarvisShapes] through the composition hierarchy.
 */
val LocalJarvisShapes = staticCompositionLocalOf {
    JarvisShapes()
}
