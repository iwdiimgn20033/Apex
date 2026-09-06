package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemePreset(val title: String, val titleAr: String, val description: String, val descriptionAr: String) {
    APEX_CYAN_PRO(
        title = "Apex Sky & Cyan",
        titleAr = "سماوي ديناميكي فاقع (Apex Sky)",
        description = "Vibrant electric azure & luminous sky canvas",
        descriptionAr = "أزرق سماوي كهربائي نابض بالحياة مع خلفية بلورية فاتحة"
    ),
    EMERALD_TRADER(
        title = "Emerald & Mint",
        titleAr = "زمردي ونعناعي مشرق (Mint Trader)",
        description = "Lush energetic growth mint & jade highlights",
        descriptionAr = "أخضر زمردي ونعناعي مشرق مع تباين عالي"
    ),
    BLOOMBERG_GOLD(
        title = "Solar Amber Gold",
        titleAr = "كهرماني وذهبي مشع (Solar Amber)",
        description = "Radiant golden sunshine & warm luminous cards",
        descriptionAr = "ألوان شمسية كهرمانية ذهبية دافئة ومشرقة"
    ),
    VIOLET_PULSE(
        title = "Iris & Lavender",
        titleAr = "بنفسجي ولافندر حيوي (Iris Pulse)",
        description = "Electric violet & luminous pastel lavender",
        descriptionAr = "بنفسجي كهربائي أنيق مع بطاقات لافندر باستيل فاتحة"
    ),
    CLEAN_LIGHT(
        title = "Coral & Rose",
        titleAr = "مرجاني ووردي ديناميكي (Coral Momentum)",
        description = "Dynamic energetic coral pink & pearl surface",
        descriptionAr = "وردي مرجاني حيوي وطاقة ديناميكية فائقة الوضوح"
    )
}

// ==========================================
// VIBRANT DYNAMIC LIGHT COLOR SCHEMES
// ==========================================

// 1. Apex Sky & Cyan Dynamic Light
private val DynamicSkyLightColorScheme = lightColorScheme(
    primary = DynamicSkyPrimary,
    onPrimary = Color.White,
    primaryContainer = DynamicSkyContainer,
    onPrimaryContainer = DynamicSkyOnContainer,
    secondary = BullishGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = DynamicAmberPrimary,
    onTertiary = Color.White,
    background = DynamicSkyBackground,
    onBackground = DynamicTextPrimaryLight,
    surface = DynamicSkySurface,
    onSurface = DynamicTextPrimaryLight,
    surfaceVariant = DynamicSkySurfaceVariant,
    onSurfaceVariant = DynamicTextSecondaryLight,
    outline = DynamicSkyOutline,
    error = BearishRed,
    onError = Color.White
)

// 2. Emerald Mint Dynamic Light
private val DynamicMintLightColorScheme = lightColorScheme(
    primary = DynamicMintPrimary,
    onPrimary = Color.White,
    primaryContainer = DynamicMintContainer,
    onPrimaryContainer = DynamicMintOnContainer,
    secondary = DynamicSkyPrimary,
    onSecondary = Color.White,
    secondaryContainer = DynamicSkyContainer,
    onSecondaryContainer = DynamicSkyOnContainer,
    tertiary = DynamicAmberPrimary,
    onTertiary = Color.White,
    background = DynamicMintBackground,
    onBackground = Color(0xFF064E3B),
    surface = DynamicMintSurface,
    onSurface = Color(0xFF064E3B),
    surfaceVariant = DynamicMintSurfaceVariant,
    onSurfaceVariant = Color(0xFF334155),
    outline = DynamicMintOutline,
    error = BearishRed,
    onError = Color.White
)

// 3. Solar Amber Dynamic Light
private val DynamicAmberLightColorScheme = lightColorScheme(
    primary = DynamicAmberPrimary,
    onPrimary = Color.White,
    primaryContainer = DynamicAmberContainer,
    onPrimaryContainer = DynamicAmberOnContainer,
    secondary = DynamicMintPrimary,
    onSecondary = Color.White,
    secondaryContainer = DynamicMintContainer,
    onSecondaryContainer = DynamicMintOnContainer,
    tertiary = DynamicSkyPrimary,
    onTertiary = Color.White,
    background = DynamicAmberBackground,
    onBackground = Color(0xFF1C1917),
    surface = DynamicAmberSurface,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = DynamicAmberSurfaceVariant,
    onSurfaceVariant = Color(0xFF44403C),
    outline = DynamicAmberOutline,
    error = BearishRed,
    onError = Color.White
)

// 4. Iris Lavender Dynamic Light
private val DynamicIrisLightColorScheme = lightColorScheme(
    primary = DynamicIrisPrimary,
    onPrimary = Color.White,
    primaryContainer = DynamicIrisContainer,
    onPrimaryContainer = DynamicIrisOnContainer,
    secondary = DynamicSkyPrimary,
    onSecondary = Color.White,
    secondaryContainer = DynamicSkyContainer,
    onSecondaryContainer = DynamicSkyOnContainer,
    tertiary = DynamicMintPrimary,
    onTertiary = Color.White,
    background = DynamicIrisBackground,
    onBackground = Color(0xFF1E1B4B),
    surface = DynamicIrisSurface,
    onSurface = Color(0xFF1E1B4B),
    surfaceVariant = DynamicIrisSurfaceVariant,
    onSurfaceVariant = Color(0xFF4C1D95),
    outline = DynamicIrisOutline,
    error = BearishRed,
    onError = Color.White
)

// 5. Coral Rose Dynamic Light
private val DynamicCoralLightColorScheme = lightColorScheme(
    primary = DynamicCoralPrimary,
    onPrimary = Color.White,
    primaryContainer = DynamicCoralContainer,
    onPrimaryContainer = DynamicCoralOnContainer,
    secondary = DynamicSkyPrimary,
    onSecondary = Color.White,
    secondaryContainer = DynamicSkyContainer,
    onSecondaryContainer = DynamicSkyOnContainer,
    tertiary = DynamicAmberPrimary,
    onTertiary = Color.White,
    background = DynamicCoralBackground,
    onBackground = Color(0xFF1F1215),
    surface = DynamicCoralSurface,
    onSurface = Color(0xFF1F1215),
    surfaceVariant = DynamicCoralSurfaceVariant,
    onSurfaceVariant = Color(0xFF4C1D28),
    outline = DynamicCoralOutline,
    error = BearishRed,
    onError = Color.White
)

// ==========================================
// DARK COLOR SCHEMES (Preserved for Dark Mode Toggle)
// ==========================================
private val ApexProDarkColorScheme = darkColorScheme(
    primary = ApexCyanPrimary,
    onPrimary = Color(0xFF001F28),
    primaryContainer = ApexCyanContainer,
    onPrimaryContainer = ApexCyanOnContainer,
    secondary = BullishGreenDark,
    onSecondary = Color(0xFF003915),
    secondaryContainer = Color(0xFF004D1F),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = ApexGold,
    onTertiary = Color(0xFF3E2800),
    background = ApexBgDark,
    onBackground = ApexTextPrimaryDark,
    surface = ApexSurfaceDark,
    onSurface = ApexTextPrimaryDark,
    surfaceVariant = ApexSurfaceVariantDark,
    onSurfaceVariant = ApexTextSecondaryDark,
    outline = ApexOutlineDark,
    error = BearishRedDark,
    onError = Color.White
)

private val EmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF003822),
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = ApexCyanPrimary,
    onSecondary = Color(0xFF001F28),
    secondaryContainer = ApexCyanContainer,
    onSecondaryContainer = ApexCyanOnContainer,
    tertiary = ApexGold,
    onTertiary = Color(0xFF3E2800),
    background = Color(0xFF07120D),
    onBackground = ApexTextPrimaryDark,
    surface = Color(0xFF0E1F18),
    onSurface = ApexTextPrimaryDark,
    surfaceVariant = Color(0xFF162E24),
    onSurfaceVariant = ApexTextSecondaryDark,
    outline = Color(0xFF224738),
    error = BearishRedDark,
    onError = Color.White
)

private val TerminalGoldDarkColorScheme = darkColorScheme(
    primary = TerminalGoldPrimary,
    onPrimary = Color(0xFF3F2E00),
    primaryContainer = TerminalGoldContainer,
    onPrimaryContainer = TerminalGoldOnContainer,
    secondary = BullishGreenDark,
    onSecondary = Color(0xFF003915),
    secondaryContainer = Color(0xFF004D1F),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = ApexCyanPrimary,
    onTertiary = Color(0xFF001F28),
    background = Color(0xFF0A0A0A),
    onBackground = ApexTextPrimaryDark,
    surface = Color(0xFF141414),
    onSurface = ApexTextPrimaryDark,
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = ApexTextSecondaryDark,
    outline = Color(0xFF333333),
    error = BearishRedDark,
    onError = Color.White
)

private val VioletDarkColorScheme = darkColorScheme(
    primary = VioletDark,
    onPrimary = Color(0xFF250262),
    primaryContainer = VioletContainer,
    onPrimaryContainer = VioletOnContainer,
    secondary = BullishGreenDark,
    onSecondary = Color(0xFF003915),
    secondaryContainer = Color(0xFF004D1F),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = ApexCyanPrimary,
    onTertiary = Color(0xFF001F28),
    background = Color(0xFF0E0B16),
    onBackground = ApexTextPrimaryDark,
    surface = Color(0xFF171324),
    onSurface = ApexTextPrimaryDark,
    surfaceVariant = Color(0xFF221C34),
    onSurfaceVariant = ApexTextSecondaryDark,
    outline = Color(0xFF382F52),
    error = BearishRedDark,
    onError = Color.White
)

@Composable
fun ApexBrokerTheme(
    preset: ThemePreset = ThemePreset.APEX_CYAN_PRO,
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        !darkTheme -> when (preset) {
            ThemePreset.APEX_CYAN_PRO -> DynamicSkyLightColorScheme
            ThemePreset.EMERALD_TRADER -> DynamicMintLightColorScheme
            ThemePreset.BLOOMBERG_GOLD -> DynamicAmberLightColorScheme
            ThemePreset.VIOLET_PULSE -> DynamicIrisLightColorScheme
            ThemePreset.CLEAN_LIGHT -> DynamicCoralLightColorScheme
        }
        preset == ThemePreset.APEX_CYAN_PRO -> ApexProDarkColorScheme
        preset == ThemePreset.EMERALD_TRADER -> EmeraldDarkColorScheme
        preset == ThemePreset.BLOOMBERG_GOLD -> TerminalGoldDarkColorScheme
        preset == ThemePreset.VIOLET_PULSE -> VioletDarkColorScheme
        preset == ThemePreset.CLEAN_LIGHT -> DynamicCoralLightColorScheme
        else -> ApexProDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}



