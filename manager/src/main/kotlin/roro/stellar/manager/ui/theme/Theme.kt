package roro.stellar.manager.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import roro.stellar.manager.compat.BuildUtils.atLeast31

private val LightColorScheme = lightColorScheme()
private val DarkColorScheme = darkColorScheme()

// Shared shape and type scale; Material components and custom Stellar cards use
// the same rounded, expressive visual language rather than unrelated radii.
private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val ExpressiveTypography = Typography().let { standard ->
    Typography(
        headlineLarge = standard.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = standard.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = standard.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = standard.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}

private fun Context.activityOrNull(): Activity? {
    var context: Context? = this
    while (context != null) {
        if (context is Activity) return context
        context = (context as? ContextWrapper)?.baseContext
    }
    return null
}

@Composable
fun StellarTheme(
    themeMode: ThemeMode = ThemePreferences.themeMode.value,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && atLeast31 ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    val activity = remember(context) { context.activityOrNull() }
    if (!view.isInEditMode && activity != null) {
        SideEffect {
            val window = activity.window
            // Android 15+ edge-to-edge: let Compose paint behind both system bars.
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        shapes = ExpressiveShapes,
        typography = ExpressiveTypography,
        content = content
    )
}
