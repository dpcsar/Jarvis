package site.jarviscopilot.jarvis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val JarvisLightColorScheme = lightColorScheme(
    primary = JarvisLightBlue,
    secondary = JarvisLightTeal,
    tertiary = JarvisLightAccent,
    background = JarvisLightBackground,
    surface = JarvisLightSurface,
    error = JarvisLightEmergency,
    errorContainer = JarvisLightEmergencyBackground, // Added emergency background color
    onPrimary = JarvisOnPrimary,
    onSecondary = JarvisOnSecondary,
    onTertiary = JarvisOnTertiary,
    onBackground = JarvisDarkGray,
    onSurface = JarvisDarkGray,
    onError = JarvisOnPrimary
)

private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisBlue,
    secondary = JarvisTeal,
    tertiary = JarvisAccent,
    background = JarvisDarkBackground,
    surface = JarvisDarkSurface,
    error = JarvisDarkEmergency,
    errorContainer = JarvisDarkEmergencyBackground, // Added emergency background color
    onPrimary = JarvisOnPrimary,
    onSecondary = JarvisOnSecondary,
    onTertiary = JarvisOnTertiary,
    onBackground = JarvisAccent,
    onSurface = JarvisGray,
    onError = JarvisOnPrimary
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> JarvisDarkColorScheme
        else -> JarvisLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
