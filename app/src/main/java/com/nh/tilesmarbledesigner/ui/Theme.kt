package com.nh.tilesmarbledesigner.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * One colour family per theme (pastel background, lighter cards, mid-tone accent for titles/icons,
 * a deep colour for switches/buttons/selection rings).
 */
data class NHPalette(
    val id: String, val name: String,
    val bg: Color, val card: Color, val chip: Color,
    val primary: Color, val dark: Color, val soft: Color, val ink: Color,
    val isDark: Boolean = false
)

object Palettes {
    val all = listOf(
        NHPalette("forest", "Forest Whisper", Color(0xFFE8F1E2), Color(0xFFF3F8EE), Color(0xFFDCEBD3), Color(0xFF3E7D2A), Color(0xFF1B5E20), Color(0xFFC5E4BD), Color(0xFF1E4A14)),
        NHPalette("lavender", "Lavender Dream", Color(0xFFEEE3F7), Color(0xFFF7EFFB), Color(0xFFE2D2F1), Color(0xFF9C4FDB), Color(0xFF6A38B0), Color(0xFFDCC8EE), Color(0xFF4A2278)),
        NHPalette("ocean", "Ocean Waves", Color(0xFFDFF5F5), Color(0xFFEBFAFA), Color(0xFFC9ECEB), Color(0xFF17A398), Color(0xFF5C5CC4), Color(0xFFB2E4EC), Color(0xFF0B4F4A)),
        NHPalette("sunset", "Sunset Glow", Color(0xFFFAE8D4), Color(0xFFFDF3E7), Color(0xFFF6D9BA), Color(0xFFF29B4B), Color(0xFFD84A0F), Color(0xFFF6D6A8), Color(0xFF6E3007)),
        NHPalette("rose", "Rose Petal", Color(0xFFFCE4EC), Color(0xFFFEF0F4), Color(0xFFF8C9D9), Color(0xFFEC407A), Color(0xFFC2185B), Color(0xFFF6BCD0), Color(0xFF7A0E3C)),
        NHPalette("slate", "Misty Slate", Color(0xFFF0F0F0), Color(0xFFFAFAFA), Color(0xFFE0E0E6), Color(0xFF6B7280), Color(0xFF6E6EC8), Color(0xFFC9CAD6), Color(0xFF2B3340)),
        NHPalette("midnight", "Midnight Gold", Color(0xFF0B1020), Color(0xFF16233F), Color(0xFF1F2F52), Color(0xFFD4AF37), Color(0xFF1F6FFF), Color(0xFF3A4A6B), Color(0xFFF2F2F2), isDark = true)
    )

    fun byId(id: String?): NHPalette = all.firstOrNull { it.id == id } ?: all[0]
}

val LocalPalette = staticCompositionLocalOf { Palettes.all[0] }

/** Accent that always has good contrast on the page background (deep colour on light themes, gold on dark). */
val NHPalette.accent: Color get() = if (isDark) primary else dark

/** Muted theme-tinted colour for labels, captions and sub-text (mix of the theme's mid tone and its ink). */
val NHPalette.label: Color get() = lerp(primary, ink, 0.5f)

/** Strong theme colour for key numbers and highlights (mix of mid tone and deep tone). */
val NHPalette.highlight: Color get() = if (isDark) primary else lerp(primary, dark, 0.45f)

// Palette-aware colour accessors (names kept so every screen follows the active theme).
val Gold: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.accent      // readable accent: titles / icons
val Blue: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.dark        // buttons / selection
val Navy: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.bg          // page background
val NavySurface: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.card // cards
val NavyCard: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.chip    // fields / chips
val Ink: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.ink          // body text
val Danger = Color(0xFFE5484D)

private tailrec fun Context.findAct(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findAct()
    else -> null
}

@Composable
private fun SystemBars(p: NHPalette) {
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val w = view.context.findAct()?.window ?: return@SideEffect
        w.statusBarColor = p.bg.toArgb()
        w.navigationBarColor = p.card.toArgb()
        val c = WindowCompat.getInsetsController(w, view)
        c.isAppearanceLightStatusBars = !p.isDark
        c.isAppearanceLightNavigationBars = !p.isDark
    }
}

private val base = Typography()
private val NHType = Typography(
    bodyLarge = base.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = base.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = base.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = base.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = base.labelMedium.copy(fontSize = 12.sp),
    titleMedium = base.titleMedium.copy(fontSize = 15.sp),
    titleLarge = base.titleLarge.copy(fontSize = 20.sp)
)

@Composable
fun NHTheme(palette: NHPalette, content: @Composable () -> Unit) {
    val scheme = if (palette.isDark) darkColorScheme(
        primary = palette.dark, onPrimary = Color.White, primaryContainer = palette.chip, onPrimaryContainer = palette.ink,
        secondary = palette.primary, onSecondary = Color.Black, secondaryContainer = palette.chip, onSecondaryContainer = palette.ink,
        background = palette.bg, onBackground = palette.ink, surface = palette.card, onSurface = palette.ink,
        surfaceVariant = palette.chip, onSurfaceVariant = palette.label,
        outline = palette.ink.copy(alpha = 0.45f), outlineVariant = palette.ink.copy(alpha = 0.15f), error = Danger
    ) else lightColorScheme(
        primary = palette.dark, onPrimary = Color.White, primaryContainer = palette.chip, onPrimaryContainer = palette.ink,
        secondary = palette.primary, onSecondary = Color.White, secondaryContainer = palette.chip, onSecondaryContainer = palette.ink,
        background = palette.bg, onBackground = palette.ink, surface = palette.card, onSurface = palette.ink,
        surfaceVariant = palette.chip, onSurfaceVariant = palette.label,
        outline = palette.ink.copy(alpha = 0.45f), outlineVariant = palette.ink.copy(alpha = 0.15f), error = Danger
    )
    CompositionLocalProvider(LocalPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = NHType,
            shapes = Shapes(
                small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(14.dp),
                large = RoundedCornerShape(20.dp), extraLarge = RoundedCornerShape(26.dp)
            )
        ) {
            SystemBars(palette)
            content()
        }
    }
}
