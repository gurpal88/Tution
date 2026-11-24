package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Immutable
data class TuitionStatusPalette(
    val paid: Color,
    val paidContainer: Color,
    val pending: Color,
    val pendingContainer: Color,
    val overdue: Color,
    val overdueContainer: Color,
    val isDark: Boolean
)

val LocalTuitionStatusPalette = staticCompositionLocalOf {
    TuitionStatusPalette(
        paid = FeePaidGreen,
        paidContainer = FeePaidContainer,
        pending = FeePendingAmber,
        pendingContainer = FeePendingContainer,
        overdue = FeeOverdueRed,
        overdueContainer = FeeOverdueContainer,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = SapphirePrimary,
    onPrimary = Color.White,
    primaryContainer = IceBlueContainer,
    onPrimaryContainer = SapphireDeep,
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = MintContainer,
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary = AmberWarm,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = Color(0xFF451A03),
    error = FeeOverdueRed,
    onError = Color.White,
    errorContainer = FeeOverdueContainer,
    onErrorContainer = Color(0xFF7F1D1D),
    background = IvoryBackground,
    onBackground = SapphireDeep,
    surface = PureSurface,
    onSurface = SapphireDeep,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = Color(0xFF4B5B78),
    outline = Color(0xFF94A3B8),
    outlineVariant = SubtleOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkSapphirePrimary,
    onPrimary = SapphireDeep,
    primaryContainer = DarkSapphireContainer,
    onPrimaryContainer = IceBlueContainer,
    secondary = DarkEmeraldAccent,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = DarkEmeraldContainer,
    onSecondaryContainer = MintContainer,
    tertiary = FeePendingAmberDark,
    onTertiary = Color(0xFF3E2908),
    tertiaryContainer = FeePendingContainerDark,
    onTertiaryContainer = AmberContainerLight,
    error = FeeOverdueRedDark,
    onError = Color(0xFF450A0A),
    errorContainer = FeeOverdueContainerDark,
    onErrorContainer = FeeOverdueContainer,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA8B5CF),
    outline = Color(0xFF64748B),
    outlineVariant = DarkOutline
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TuitionManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val statusPalette = if (darkTheme) {
        TuitionStatusPalette(
            paid = FeePaidGreenDark,
            paidContainer = FeePaidContainerDark,
            pending = FeePendingAmberDark,
            pendingContainer = FeePendingContainerDark,
            overdue = FeeOverdueRedDark,
            overdueContainer = FeeOverdueContainerDark,
            isDark = true
        )
    } else {
        TuitionStatusPalette(
            paid = FeePaidGreen,
            paidContainer = FeePaidContainer,
            pending = FeePendingAmber,
            pendingContainer = FeePendingContainer,
            overdue = FeeOverdueRed,
            overdueContainer = FeeOverdueContainer,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalTuitionStatusPalette provides statusPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
