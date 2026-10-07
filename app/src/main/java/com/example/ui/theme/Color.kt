package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Core Colors
val BrandPurple = Color(0xFF6C5CE7)
val BrandCyan = Color(0xFF00C2FF)
val BrandEmerald = Color(0xFF00E5A8)
val BrandPink = Color(0xFFFF4D8D)
val BrandViolet = Color(0xFF7C4DFF)

// Dark Theme Surfaces
val BackgroundDark = Color(0xFF070B17)
val SurfaceDark = Color(0xFF0E1424)
val CardDark = Color(0xFF111827)
val CardSurfaceElevated = Color(0xFF162035)
val CardBorderDark = Color(0xFF1F2D4A)

// Light Theme Surfaces
val BackgroundLight = Color(0xFFF3F5FA)
val SurfaceLight = Color(0xFFFFFFFF)
val CardLight = Color(0xFFFFFFFF)
val CardBorderLight = Color(0xFFE2E8F0)

// Text Colors
val TextWhite = Color(0xFFFFFFFF)
val TextLightMode = Color(0xFF0F172A)
val TextSecondary = Color(0xFFA7B0C0)
val TextSecondaryLight = Color(0xFF64748B)
val TextDim = Color(0xFF62728B)

// Accents / Status
val StatusRunning = Color(0xFF00E5A8)
val StatusPaused = Color(0xFFFFB800)
val StatusStopped = Color(0xFFFF4D8D)
val GoldBestLap = Color(0xFFFFD700)

// Custom Gradients
val PrimaryGradientBrush = Brush.linearGradient(
    colors = listOf(BrandPurple, BrandCyan, BrandEmerald)
)

val SecondaryGradientBrush = Brush.linearGradient(
    colors = listOf(BrandPink, BrandViolet)
)

val CardGlowGradientBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF1B2845), Color(0xFF0E1626))
)

val StopwatchRingBrush = Brush.sweepGradient(
    colors = listOf(
        BrandPurple,
        BrandCyan,
        BrandEmerald,
        BrandPink,
        BrandPurple
    )
)

val ButtonStartGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF00C2FF), Color(0xFF00E5A8))
)

val ButtonPauseGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF8A00), Color(0xFFFF4D8D))
)

val ButtonSecondaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6C5CE7), Color(0xFF7C4DFF))
)

val LapCardBestGradient = Brush.horizontalGradient(
    colors = listOf(Color(0x2200E5A8), Color(0x1000C2FF))
)
