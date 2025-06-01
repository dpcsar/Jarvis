package site.jarviscopilot.jarvis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Create light color scheme with reference colors instead of error
private val JarvisLightColorScheme = lightColorScheme(
    primary = JarvisLightBlue,
    secondary = JarvisLightTeal,
    tertiary = JarvisLightAccent,
    background = JarvisLightBackground,
    surface = JarvisLightSurface,
    error = JarvisLightReference, // Keep for Material compatibility
    errorContainer = JarvisLightReferenceBackground, // Keep for Material compatibility
    onPrimary = JarvisOnPrimary,
    onSecondary = JarvisOnSecondary,
    onTertiary = JarvisOnTertiary,
    onBackground = JarvisDarkGray,
    onSurface = JarvisDarkGray,
    onError = JarvisOnPrimary, // Keep for Material compatibility
)

// Create dark color scheme with reference colors instead of error
private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisBlue,
    secondary = JarvisTeal,
    tertiary = JarvisAccent,
    background = JarvisDarkBackground,
    surface = JarvisDarkSurface,
    error = JarvisDarkReference, // Keep for Material compatibility
    errorContainer = JarvisDarkReferenceBackground, // Keep for Material compatibility
    onPrimary = JarvisOnPrimary,
    onSecondary = JarvisOnSecondary,
    onTertiary = JarvisOnTertiary,
    onBackground = JarvisAccent,
    onSurface = JarvisGray,
    onError = JarvisOnPrimary, // Keep for Material compatibility
)

// Create Jarvis light color scheme with reference, emergency, warning, and caution colors
private val JarvisLightExtendedColorScheme = JarvisColorScheme(
    colorScheme = JarvisLightColorScheme,
    reference = JarvisLightReference,
    referenceContainer = JarvisLightReferenceBackground,
    onReference = JarvisOnPrimary,
    onReferenceContainer = JarvisDarkGray,
    emergency = JarvisLightEmergency,
    emergencyContainer = JarvisLightEmergencyBackground,
    onEmergency = JarvisOnPrimary,
    onEmergencyContainer = JarvisDarkGray,
    warning = JarvisLightWarning,
    warningContainer = JarvisLightWarningBackground,
    onWarning = JarvisOnPrimary,
    onWarningContainer = JarvisDarkGray,
    caution = JarvisLightCaution,
    cautionContainer = JarvisLightCautionBackground,
    onCaution = JarvisOnSecondary,
    onCautionContainer = JarvisDarkGray
)

// Create Jarvis dark color scheme with reference, emergency, warning, and caution colors
private val JarvisDarkExtendedColorScheme = JarvisColorScheme(
    colorScheme = JarvisDarkColorScheme,
    reference = JarvisDarkReference,
    referenceContainer = JarvisDarkReferenceBackground,
    onReference = JarvisOnPrimary,
    onReferenceContainer = Color.White,
    emergency = JarvisDarkEmergency,
    emergencyContainer = JarvisDarkEmergencyBackground,
    onEmergency = JarvisOnPrimary,
    onEmergencyContainer = Color.White,
    warning = JarvisDarkWarning,
    warningContainer = JarvisDarkWarningBackground,
    onWarning = JarvisOnSecondary,
    onWarningContainer = Color.White,
    caution = JarvisDarkCaution,
    cautionContainer = JarvisDarkCautionBackground,
    onCaution = JarvisOnSecondary,
    onCautionContainer = Color.White
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val materialColorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> JarvisDarkColorScheme
        else -> JarvisLightColorScheme
    }

    // Create the extended color scheme with reference, emergency, warning, and caution colors
    val jarvisColorScheme = when {
        dynamicColor -> {
            // For dynamic color, create a custom color scheme with the dynamic base
            if (darkTheme) {
                JarvisColorScheme(
                    colorScheme = materialColorScheme,
                    reference = JarvisDarkReference,
                    referenceContainer = JarvisDarkReferenceBackground,
                    onReference = JarvisOnPrimary,
                    onReferenceContainer = Color.White,
                    emergency = JarvisDarkEmergency,
                    emergencyContainer = JarvisDarkEmergencyBackground,
                    onEmergency = JarvisOnPrimary,
                    onEmergencyContainer = Color.White,
                    warning = JarvisDarkWarning,
                    warningContainer = JarvisDarkWarningBackground,
                    onWarning = JarvisOnSecondary,
                    onWarningContainer = Color.White,
                    caution = JarvisDarkCaution,
                    cautionContainer = JarvisDarkCautionBackground,
                    onCaution = JarvisOnSecondary,
                    onCautionContainer = Color.White
                )
            } else {
                JarvisColorScheme(
                    colorScheme = materialColorScheme,
                    reference = JarvisLightReference,
                    referenceContainer = JarvisLightReferenceBackground,
                    onReference = JarvisOnPrimary,
                    onReferenceContainer = JarvisDarkGray,
                    emergency = JarvisLightEmergency,
                    emergencyContainer = JarvisLightEmergencyBackground,
                    onEmergency = JarvisOnPrimary,
                    onEmergencyContainer = JarvisDarkGray,
                    warning = JarvisLightWarning,
                    warningContainer = JarvisLightWarningBackground,
                    onWarning = JarvisOnPrimary,
                    onWarningContainer = JarvisDarkGray,
                    caution = JarvisLightCaution,
                    cautionContainer = JarvisLightCautionBackground,
                    onCaution = JarvisOnSecondary,
                    onCautionContainer = JarvisDarkGray
                )
            }
        }

        darkTheme -> JarvisDarkExtendedColorScheme
        else -> JarvisLightExtendedColorScheme
    }

    // Create Jarvis typography wrapper around Material typography
    val jarvisTypography = JarvisTypography(
        materialTypography = Typography
    )

    // Create custom Jarvis shapes
    val jarvisShapes = JarvisShapes()

    // Provide both the Jarvis custom theme tokens and Material theme
    CompositionLocalProvider(
        LocalJarvisColorScheme provides jarvisColorScheme,
        LocalJarvisTypography provides jarvisTypography,
        LocalJarvisShapes provides jarvisShapes
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Expose the custom theme elements via JarvisTheme object
object JarvisTheme {
    val colorScheme: JarvisColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalJarvisColorScheme.current

    val typography: JarvisTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalJarvisTypography.current

    val shapes: JarvisShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalJarvisShapes.current
}
