package com.example.nightscreen.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

// Motion tokens: short, subtle, interruptible. Durations follow Material's
// motion guidance (press 80-120ms, state 150-220ms, transition 200-300ms).

object Motion {
    const val DurationPress = 90
    const val DurationState = 180
    const val DurationScreen = 260

    val EasingStandard = FastOutSlowInEasing
    val EasingDecelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val EasingAccelerate = LinearOutSlowInEasing

    val Press: AnimationSpec<Float> = tween(DurationPress, easing = EasingStandard)
    val State: AnimationSpec<Float> = tween(DurationState, easing = EasingStandard)
    val Screen: AnimationSpec<Float> = tween(DurationScreen, easing = EasingStandard)
}

/** True when the user asked for reduced motion (Settings -> Appearance). */
val LocalReduceMotion = staticCompositionLocalOf { false }

/** Resolves a duration against the reduce-motion preference. */
@Composable
fun motionDuration(baseMs: Int): Int = if (LocalReduceMotion.current) 0 else baseMs
