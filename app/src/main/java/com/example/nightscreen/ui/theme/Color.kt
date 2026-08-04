package com.example.nightscreen.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Night Screen brand palette.
//
// Semantic naming maps onto Material 3 color scheme slots:
//   AccentWarm / AccentWarmContainer  -> primary / primaryContainer  (amber)
//   BackgroundPrimary / BackgroundSecondary -> background / surface
//   SurfaceRaised / SurfaceInteractive -> surfaceContainer / surfaceVariant
//   ContentPrimary / ContentSecondary / ContentDisabled -> onSurface /
//       onSurfaceVariant / onSurface.copy(alpha)
//   DividerSubtle -> outlineVariant
//   StateActive -> primary      StateWarning -> Warning   StateError -> error
//
// The default theme is dark, low-luminance and warm-neutral so the app is
// comfortable to use at night. Amber is used sparingly as the action accent.
// ---------------------------------------------------------------------------

// --- Dark scheme (default; nighttime-first) ---
val DarkBackground = Color(0xFF0E1113)          // deep blue-grey, never pure black
val DarkOnBackground = Color(0xFFE1E5E9)
val DarkSurface = Color(0xFF14181B)
val DarkOnSurface = Color(0xFFE1E5E9)
val DarkSurfaceVariant = Color(0xFF1D2227)
val DarkOnSurfaceVariant = Color(0xFFA8AFB7)
val DarkSurfaceContainer = Color(0xFF191E22)
val DarkSurfaceContainerHigh = Color(0xFF20262B)
val DarkSurfaceContainerHighest = Color(0xFF272E34)

val DarkPrimary = Color(0xFFFFB454)             // warm amber accent
val DarkOnPrimary = Color(0xFF241A05)
val DarkPrimaryContainer = Color(0xFF3C2E13)
val DarkOnPrimaryContainer = Color(0xFFFFDEB0)

val DarkSecondary = Color(0xFFC9C4B8)           // warm grey
val DarkOnSecondary = Color(0xFF2B2721)
val DarkSecondaryContainer = Color(0xFF3B362C)
val DarkOnSecondaryContainer = Color(0xFFE9E2D4)

val DarkTertiary = Color(0xFF9CC9C0)            // muted teal, used for calm info
val DarkOnTertiary = Color(0xFF07251F)
val DarkTertiaryContainer = Color(0xFF173B34)
val DarkOnTertiaryContainer = Color(0xFFB8E7DD)

val DarkError = Color(0xFFFF8A80)
val DarkOnError = Color(0xFF3B0B07)
val DarkErrorContainer = Color(0xFF4C1E1A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkOutline = Color(0xFF48505A)
val DarkOutlineVariant = Color(0xFF2B323A)

// Warning is not an M3 slot; exposed as a top-level token used explicitly.
val DarkWarning = Color(0xFFFFC861)
val DarkOnWarning = Color(0xFF2B1A00)
val DarkWarningContainer = Color(0xFF45320F)
val DarkOnWarningContainer = Color(0xFFFFE3B0)

// --- Light scheme (system theme / light mode) ---
val LightBackground = Color(0xFFFAF7F1)         // warm paper
val LightOnBackground = Color(0xFF1C1B19)
val LightSurface = Color(0xFFFDFBF7)
val LightOnSurface = Color(0xFF1C1B19)
val LightSurfaceVariant = Color(0xFFEDE8DF)
val LightOnSurfaceVariant = Color(0xFF4E4A43)
val LightSurfaceContainer = Color(0xFFF3EFE7)
val LightSurfaceContainerHigh = Color(0xFFEDE9E1)
val LightSurfaceContainerHighest = Color(0xFFE7E3DB)

val LightPrimary = Color(0xFF8F5B00)            // accessible amber-brown
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFFDFAE)
val LightOnPrimaryContainer = Color(0xFF2C1A00)

val LightSecondary = Color(0xFF6F6555)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFF6E9D5)
val LightOnSecondaryContainer = Color(0xFF282014)

val LightTertiary = Color(0xFF406E64)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFC1F4E8)
val LightOnTertiaryContainer = Color(0xFF00211C)

val LightError = Color(0xFFB3261E)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF9DEDC)
val LightOnErrorContainer = Color(0xFF410E0B)

val LightOutline = Color(0xFF77736B)
val LightOutlineVariant = Color(0xFFC9C4BA)

val LightWarning = Color(0xFF9A5B00)
val LightOnWarning = Color(0xFFFFFFFF)
val LightWarningContainer = Color(0xFFFFDDB4)
val LightOnWarningContainer = Color(0xFF2C1900)

// --- Static accents (independent of theme) ---
val SuccessGreen = Color(0xFF4CAF50)
