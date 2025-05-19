package site.jarviscopilot.jarvis.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    surface = Color(0xFFF8F8F8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

data class AviationColors(
    val avBlue: Color,
    val avDarkBlue: Color,
    val avRed: Color,
    val avYellow: Color,
    val avGreen: Color,
    val avDarkGrey: Color,
    val avLightGrey: Color,
    val avBlack: Color,
    val avWhite: Color,
    val avAmber: Color,
    val textOnBackground: Color,
    val textOnSurface: Color,
    val headerBackground: Color,
    val itemBackground: Color,
    val selectedItemBackground: Color,
    val avTextWhite: Color,
)

val LightAviationColors = AviationColors(
    avBlue = AvBlue,
    avDarkBlue = AvDarkBlue,
    avRed = AvRed,
    avYellow = AvYellow,
    avGreen = AvGreen,
    avDarkGrey = AvDarkGrey,
    avLightGrey = AvLightGrey,
    avBlack = AvBlack,
    avWhite = AvWhite,
    avAmber = AvAmber,
    textOnBackground = Color.Black,
    textOnSurface = Color.Black,
    headerBackground = AvLightGrey,
    itemBackground = Color.White,
    selectedItemBackground = AvLightGrey,
    avTextWhite = AvWhite
)

val DarkAviationColors = AviationColors(
    avBlue = AvBlueDark,
    avDarkBlue = AvDarkBlueDark,
    avRed = AvRedDark,
    avYellow = AvYellowDark,
    avGreen = AvGreenDark,
    avDarkGrey = AvDarkGreyDark,
    avLightGrey = AvLightGreyDark,
    avBlack = AvBlackDark,
    avWhite = AvWhiteDark,
    avAmber = AvAmberDark,
    textOnBackground = Color.White,
    textOnSurface = Color.White,
    headerBackground = Color(0xFF2A2A2A),
    itemBackground = Color(0xFF1E1E1E),
    selectedItemBackground = Color(0xFF2A2A2A),
    avTextWhite = AvWhiteDark
)

val LocalAviationColors = staticCompositionLocalOf { LightAviationColors }

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
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val aviationColors = if (darkTheme) DarkAviationColors else LightAviationColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Enable edge-to-edge content
            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowCompat.getInsetsController(window, view)

            // Configure system bars
            controller.apply {
                // For our dark top bar, use light (white) status bar icons
                isAppearanceLightStatusBars = false
                // Navigation bar icons adapt to the theme
                isAppearanceLightNavigationBars = !darkTheme
            }

            // Make status bar transparent (modern Android 14+ approach)
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
        }
    }

    CompositionLocalProvider(LocalAviationColors provides aviationColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

