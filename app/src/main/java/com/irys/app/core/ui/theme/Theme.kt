package com.irys.app.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryTealDim,
    onPrimaryContainer = TextPrimary,
    secondary = SecondaryBlue,
    onSecondary = BackgroundDark,
    secondaryContainer = SecondaryBlueDim,
    onSecondaryContainer = TextPrimary,
    tertiary = EmergencyAmber,
    onTertiary = BackgroundDark,
    error = EmergencyRed,
    onError = TextPrimary,
    errorContainer = EmergencyRedDim,
    onErrorContainer = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun IrysTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = IrysTypography,
        content = content
    )
}
