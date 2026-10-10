package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode(val titleBn: String) {
    SYSTEM("সিস্টেম ডিফল্ট"),
    LIGHT("লাইট মোড"),
    DARK("ডার্ক মোড")
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = SlatePrimary,
    onPrimary = SlateOnPrimary,
    primaryContainer = SlatePrimaryContainer,
    onPrimaryContainer = SlateOnPrimaryContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = TealOnSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

@Composable
fun MathMasterTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    globalConfig: com.example.ui.customization.GlobalCustomConfig? = null,
    dynamicColor: Boolean = false, // Keep branded math theme colors crisp across devices
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply user custom theme overrides if present
    val colorScheme = if (globalConfig != null) {
        val accentColor = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customAccentHex)
        val primaryColor = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customPrimaryActionHex)

        if (darkTheme) {
            val bg = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customDarkBackgroundHex)
            val surface = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customDarkCardSurfaceHex)
            val text = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customDarkTextColorHex)
            baseScheme.copy(
                primary = primaryColor ?: baseScheme.primary,
                secondary = accentColor ?: baseScheme.secondary,
                background = bg ?: baseScheme.background,
                surface = surface ?: baseScheme.surface,
                onBackground = text ?: baseScheme.onBackground,
                onSurface = text ?: baseScheme.onSurface
            )
        } else {
            val bg = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customLightBackgroundHex)
            val surface = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customLightCardSurfaceHex)
            val text = com.example.ui.customization.ColorUtils.parseHexColor(globalConfig.customLightTextColorHex)
            baseScheme.copy(
                primary = primaryColor ?: baseScheme.primary,
                secondary = accentColor ?: baseScheme.secondary,
                background = bg ?: baseScheme.background,
                surface = surface ?: baseScheme.surface,
                onBackground = text ?: baseScheme.onBackground,
                onSurface = text ?: baseScheme.onSurface
            )
        }
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
