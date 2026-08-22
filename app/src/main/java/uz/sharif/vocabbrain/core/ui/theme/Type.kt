package uz.sharif.vocabbrain.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uz.sharif.vocabbrain.R

/** Body font of the source design. */
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

/** Display font, used for titles only. */
val Poppins = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val default = Typography()

/**
 * The named styles of the Flutter app mapped onto Material 3 slots, so stock components
 * pick them up without a second styling system:
 * `poppins24w700` → headlineLarge, `poppins18w600` → titleLarge,
 * `manrope18w600` → titleMedium, `manrope16w500` → bodyLarge,
 * `manrope14w400` → bodyMedium, `manrope12w400` → bodySmall.
 */
val VocabTypography = Typography(
    displayLarge = default.displayLarge.copy(fontFamily = Poppins),
    displayMedium = default.displayMedium.copy(fontFamily = Poppins),
    displaySmall = default.displaySmall.copy(fontFamily = Poppins),
    headlineLarge = default.headlineLarge.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
    ),
    headlineMedium = default.headlineMedium.copy(fontFamily = Poppins),
    headlineSmall = default.headlineSmall.copy(fontFamily = Poppins),
    titleLarge = default.titleLarge.copy(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    titleMedium = default.titleMedium.copy(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
    ),
    titleSmall = default.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Medium),
    bodyLarge = default.bodyLarge.copy(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyMedium = default.bodyMedium.copy(fontFamily = Manrope, fontSize = 14.sp),
    bodySmall = default.bodySmall.copy(fontFamily = Manrope, fontSize = 12.sp),
    labelLarge = default.labelLarge.copy(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    labelMedium = default.labelMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Medium),
    labelSmall = default.labelSmall.copy(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
    ),
)
