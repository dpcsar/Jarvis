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
    onPrimary = JarvisOnPrimary,
    onSecondary = JarvisOnSecondary,
    onTertiary = JarvisOnTertiary,
    onBackground = JarvisDarkGray,
    onSurface = JarvisDarkGray
)

private val JarvisDarkColorScheme = darkColorScheme(
    primary = JarvisBlue,
    secondary = JarvisTeal,
    tertiary = JarvisAccent,
    background = JarvisDarkBackground,
    surface = JarvisDarkSurface,
    onPrimary = JarvisOnPrimary,
    onSecondary = JarvisOnSecondary,
    onTertiary = JarvisOnTertiary,
    onBackground = JarvisAccent,
    onSurface = JarvisGray
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
