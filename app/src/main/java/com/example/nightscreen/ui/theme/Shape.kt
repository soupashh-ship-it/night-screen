package com.example.nightscreen.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner-radius system: restrained, consistent, premium.
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Named shape helpers used directly by components.
object CornerRadius {
    val Swatch = RoundedCornerShape(8.dp)
    val Chip = RoundedCornerShape(10.dp)
    val Card = RoundedCornerShape(16.dp)
    val Sheet = RoundedCornerShape(24.dp)
}
