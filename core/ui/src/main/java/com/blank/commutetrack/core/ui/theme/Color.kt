package com.blank.commutetrack.core.ui.theme

import androidx.compose.ui.graphics.Color

// CommuteTrack Dark Green Theme - OLED Optimized
object CommuteColors {
    val DarkestGreen = Color(0xFF0A0F0D)        // Background (OLED black-green)
    val DarkSurface = Color(0xFF111916)          // Slightly lighter surface
    val GlassyCard = Color(0xFF16201A)           // Card surface
    val BorderGreen = Color(0xFF2A3830)          // Subtle borders
    val DarkBorder = Color(0xFF1E2B24)           // Darker borders

    val NeonGreen = Color(0xFF00FF47)            // Primary accent (active states, CTAs)
    val NeonGreenDim = Color(0xFF00CC39)         // Dimmer neon green
    val SlateGreen = Color(0xFF8E9994)           // Secondary text
    val MutedGreen = Color(0xFF5A6C64)           // Muted text / completed

    val PausedAmber = Color(0xFFFFB800)          // Paused state
    val ErrorRed = Color(0xFFFF4444)             // Error / stop
    val ErrorRedDim = Color(0xFFCC3333)          // Dimmer error
}

// Status colors
val ActiveGreen = Color(0xFF00FF47)
val PausedAmber = Color(0xFFFFB800)
val CompletedBlue = Color(0xFF4A9EFF)
val CancelledRed = Color(0xFFFF4444)

// Transport mode colors
val WalkingColor = Color(0xFF66BB6A)
val CyclingColor = Color(0xFF42A5F5)
val DrivingColor = Color(0xFFEF5350)
val TransitColor = Color(0xFFAB47BC)
val MotorcycleColor = Color(0xFFFFA726)
