@file:Suppress("unused")

package site.jarviscopilot.jarvis.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * Typography system for Jarvis app, wrapping Material 3 Typography
 * while allowing for custom extensions
 */
class JarvisTypography(
    val materialTypography: Typography,

    // Standard Material 3 text styles - delegated to material typography
    val displayLarge: TextStyle = materialTypography.displayLarge,
    val displayMedium: TextStyle = materialTypography.displayMedium,
    val displaySmall: TextStyle = materialTypography.displaySmall,
    val headlineLarge: TextStyle = materialTypography.headlineLarge,
    val headlineMedium: TextStyle = materialTypography.headlineMedium,
    val headlineSmall: TextStyle = materialTypography.headlineSmall,
    val titleLarge: TextStyle = materialTypography.titleLarge,
    val titleMedium: TextStyle = materialTypography.titleMedium,
    val titleSmall: TextStyle = materialTypography.titleSmall,
    val bodyLarge: TextStyle = materialTypography.bodyLarge,
    val bodyMedium: TextStyle = materialTypography.bodyMedium,
    val bodySmall: TextStyle = materialTypography.bodySmall,
    val labelLarge: TextStyle = materialTypography.labelLarge,
    val labelMedium: TextStyle = materialTypography.labelMedium,
    val labelSmall: TextStyle = materialTypography.labelSmall

    // You can add custom text styles here if needed in the future
    // For example:
    // val emergencyText: TextStyle = TextStyle(...)
)

// Create a Local composition for providing JarvisTypography
val LocalJarvisTypography = staticCompositionLocalOf {
    JarvisTypography(
        materialTypography = Typography()
    )
}

