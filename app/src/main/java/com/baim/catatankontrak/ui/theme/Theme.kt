package com.baim.catatankontrak.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.baim.catatankontrak.data.DocColor

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F4BC7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF0E1A5C),
    secondary = Color(0xFF5B6478),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF1A1C24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C24),
    surfaceVariant = Color(0xFFEEF0F6),
    onSurfaceVariant = Color(0xFF5B6478),
    outline = Color(0xFFD5D9E4),
    error = Color(0xFFD93A3A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DB0FF),
    onPrimary = Color(0xFF0E1A5C),
    primaryContainer = Color(0xFF2A3A8F),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFFB4BCCF),
    background = Color(0xFF12141A),
    onBackground = Color(0xFFE4E6EE),
    surface = Color(0xFF1B1E26),
    onSurface = Color(0xFFE4E6EE),
    surfaceVariant = Color(0xFF252935),
    onSurfaceVariant = Color(0xFFA4ABBD),
    outline = Color(0xFF353A48),
    error = Color(0xFFFF8A80),
)

/** Palet warna baris berkas (base / hijau / kuning). */
@Immutable
data class DocPalette(
    val baseBg: Color,
    val baseBorder: Color,
    val greenBg: Color,
    val greenAccent: Color,
    val yellowBg: Color,
    val yellowAccent: Color,
) {
    fun background(c: DocColor) = when (c) {
        DocColor.BASE -> baseBg
        DocColor.GREEN -> greenBg
        DocColor.YELLOW -> yellowBg
    }

    fun accent(c: DocColor) = when (c) {
        DocColor.BASE -> baseBorder
        DocColor.GREEN -> greenAccent
        DocColor.YELLOW -> yellowAccent
    }
}

private val LightDocPalette = DocPalette(
    baseBg = Color(0xFFFFFFFF),
    baseBorder = Color(0xFFD5D9E4),
    greenBg = Color(0xFFD7F5E1),
    greenAccent = Color(0xFF1FA855),
    yellowBg = Color(0xFFFFF1C2),
    yellowAccent = Color(0xFFE0A100),
)

private val DarkDocPalette = DocPalette(
    baseBg = Color(0xFF1B1E26),
    baseBorder = Color(0xFF353A48),
    greenBg = Color(0xFF173D27),
    greenAccent = Color(0xFF4ADE80),
    yellowBg = Color(0xFF4A3B0E),
    yellowAccent = Color(0xFFFACC15),
)

val LocalDocPalette = staticCompositionLocalOf { LightDocPalette }

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
)

@Composable
fun CatatanKontrakTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    CompositionLocalProvider(LocalDocPalette provides if (dark) DarkDocPalette else LightDocPalette) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
