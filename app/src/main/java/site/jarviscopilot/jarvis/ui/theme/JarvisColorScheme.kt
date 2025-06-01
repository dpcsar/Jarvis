@file:Suppress("unused")

package site.jarviscopilot.jarvis.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extension of Material3 ColorScheme that includes additional Jarvis-specific colors
 * like reference (replacing error), emergency, warning, and caution
 */
class JarvisColorScheme(
    // Standard Material3 colors
    val colorScheme: ColorScheme,

    // Custom Jarvis colors
    val reference: Color,
    val referenceContainer: Color,
    val onReference: Color,
    val onReferenceContainer: Color,
    val emergency: Color,
    val emergencyContainer: Color,
    val onEmergency: Color,
    val onEmergencyContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarning: Color,
    val onWarningContainer: Color,
    val caution: Color,
    val cautionContainer: Color,
    val onCaution: Color,
    val onCautionContainer: Color,
) {
    // Provide the standard Material3 colors by delegation
    val primary get() = colorScheme.primary
    val onPrimary get() = colorScheme.onPrimary
    val primaryContainer get() = colorScheme.primaryContainer
    val onPrimaryContainer get() = colorScheme.onPrimaryContainer
    val secondary get() = colorScheme.secondary
    val onSecondary get() = colorScheme.onSecondary
    val secondaryContainer get() = colorScheme.secondaryContainer
    val onSecondaryContainer get() = colorScheme.onSecondaryContainer
    val tertiary get() = colorScheme.tertiary
    val onTertiary get() = colorScheme.onTertiary
    val tertiaryContainer get() = colorScheme.tertiaryContainer
    val onTertiaryContainer get() = colorScheme.onTertiaryContainer
    val background get() = colorScheme.background
    val onBackground get() = colorScheme.onBackground
    val surface get() = colorScheme.surface
    val onSurface get() = colorScheme.onSurface
    val surfaceVariant get() = colorScheme.surfaceVariant
    val onSurfaceVariant get() = colorScheme.onSurfaceVariant
    val outline get() = colorScheme.outline
    val outlineVariant get() = colorScheme.outlineVariant
    val scrim get() = colorScheme.scrim
    val surfaceTint get() = colorScheme.surfaceTint
    val inverseSurface get() = colorScheme.inverseSurface
    val inverseOnSurface get() = colorScheme.inverseOnSurface
    val inversePrimary get() = colorScheme.inversePrimary
}

// Create a Local composition for providing JarvisColorScheme
val LocalJarvisColorScheme = staticCompositionLocalOf {
    JarvisColorScheme(
        colorScheme = ColorScheme(
            primary = Color.Unspecified,
            onPrimary = Color.Unspecified,
            primaryContainer = Color.Unspecified,
            onPrimaryContainer = Color.Unspecified,
            inversePrimary = Color.Unspecified,
            secondary = Color.Unspecified,
            onSecondary = Color.Unspecified,
            secondaryContainer = Color.Unspecified,
            onSecondaryContainer = Color.Unspecified,
            tertiary = Color.Unspecified,
            onTertiary = Color.Unspecified,
            tertiaryContainer = Color.Unspecified,
            onTertiaryContainer = Color.Unspecified,
            background = Color.Unspecified,
            onBackground = Color.Unspecified,
            surface = Color.Unspecified,
            onSurface = Color.Unspecified,
            surfaceVariant = Color.Unspecified,
            onSurfaceVariant = Color.Unspecified,
            surfaceTint = Color.Unspecified,
            inverseSurface = Color.Unspecified,
            inverseOnSurface = Color.Unspecified,
            error = Color.Unspecified,
            onError = Color.Unspecified,
            errorContainer = Color.Unspecified,
            onErrorContainer = Color.Unspecified,
            outline = Color.Unspecified,
            outlineVariant = Color.Unspecified,
            scrim = Color.Unspecified,
            surfaceBright = Color.Unspecified,
            surfaceDim = Color.Unspecified,
            surfaceContainer = Color.Unspecified,
            surfaceContainerHigh = Color.Unspecified,
            surfaceContainerHighest = Color.Unspecified,
            surfaceContainerLow = Color.Unspecified,
            surfaceContainerLowest = Color.Unspecified,
        ),
        reference = Color.Unspecified,
        referenceContainer = Color.Unspecified,
        onReference = Color.Unspecified,
        onReferenceContainer = Color.Unspecified,
        emergency = Color.Unspecified,
        emergencyContainer = Color.Unspecified,
        onEmergency = Color.Unspecified,
        onEmergencyContainer = Color.Unspecified,
        warning = Color.Unspecified,
        warningContainer = Color.Unspecified,
        onWarning = Color.Unspecified,
        onWarningContainer = Color.Unspecified,
        caution = Color.Unspecified,
        cautionContainer = Color.Unspecified,
        onCaution = Color.Unspecified,
        onCautionContainer = Color.Unspecified
    )
}

