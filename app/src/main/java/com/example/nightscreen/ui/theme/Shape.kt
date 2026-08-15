package com.example.nightscreen.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner-radius system: restrained, consistent, premium.
// One scale, applied everywhere. Interactive elements go full-pill.
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Named shape helpers used directly by components. All radii belong to the
// same family: cards at 24, chips at 12, controls full-pill.
object CornerRadius {
    val Card = RoundedCornerShape(24.dp)
    val Chip = RoundedCornerShape(12.dp)
    val Sheet = RoundedCornerShape(28.dp)
    val Pill = RoundedCornerShape(100.dp)
}
