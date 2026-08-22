package uz.sharif.vocabbrain.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


@Immutable
data class VocabExtraColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val textSecondary: Color,
    val disabled: Color,
)

internal val LightExtraColors = VocabExtraColors(
    success = Success,
    warning = Warning,
    info = Info,
    textSecondary = LightTextSecondary,
    disabled = Grey90,
)

internal val DarkExtraColors = VocabExtraColors(
    success = Success,
    warning = Warning,
    info = Info,
    textSecondary = DarkTextSecondary,
    disabled = DarkDisabled,
)

internal val LocalVocabExtraColors = staticCompositionLocalOf { LightExtraColors }
