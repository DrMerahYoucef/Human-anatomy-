package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ClinicalDarkColorScheme = darkColorScheme(
    primary = SurgicalCyan,
    onPrimary = SurgicalCyanDeep,
    primaryContainer = SurgicalCyanContainer,
    onPrimaryContainer = SurgicalCyanLight,
    secondary = BioTeal,
    onSecondary = BioTealDeep,
    secondaryContainer = BioTealContainer,
    onSecondaryContainer = ClinicalIce,
    tertiary = VitalAmber,
    onTertiary = VitalAmberDeep,
    tertiaryContainer = VitalAmberContainer,
    onTertiaryContainer = ClinicalIce,
    background = SlateAbyss,
    onBackground = ClinicalIce,
    surface = SlateSurface,
    onSurface = ClinicalIce,
    surfaceVariant = SlateCard,
    onSurfaceVariant = ClinicalMuted,
    surfaceContainerLow = SlateSurface,
    surfaceContainer = SlateCard,
    surfaceContainerHigh = SlateElevated,
    surfaceContainerHighest = SlateElevated,
    outline = ClinicalSubtle,
    outlineVariant = SlateHairline,
    error = ClinicalCoral,
    onError = SlateAbyss,
)

private val ClinicalShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HumanAtlasTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ClinicalDarkColorScheme,
        typography = HumanAtlasTypography,
        shapes = ClinicalShapes,
        content = content,
    )
}
