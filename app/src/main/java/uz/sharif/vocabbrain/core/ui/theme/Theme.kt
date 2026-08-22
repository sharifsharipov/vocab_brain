package uz.sharif.vocabbrain.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPink,
    onPrimary = Color.White,
    secondary = BrandBlue,
    onSecondary = Color.White,
    tertiary = BrandViolet,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    error = BrandRed,
    onError = Color.White,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPink,
    onPrimary = Color.White,
    secondary = BrandBlue,
    onSecondary = Color.White,
    tertiary = BrandViolet,
    onTertiary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    error = BrandRed,
    onError = Color.White,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceContainerHighest,
)

/**
 * App theme. Dynamic color is deliberately off: the palette is the brand, so it must not be
 * replaced by the wallpaper colors of the device.
 */
@Composable
fun VocabbrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalVocabExtraColors provides if (darkTheme) DarkExtraColors else LightExtraColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = VocabTypography,
            shapes = VocabShapes,
            content = content,
        )
    }
}

/** Access point for the roles Material 3 does not define: `VocabTheme.colors.success`. */
object VocabTheme {
    val colors: VocabExtraColors
        @Composable @ReadOnlyComposable get() = LocalVocabExtraColors.current
}
