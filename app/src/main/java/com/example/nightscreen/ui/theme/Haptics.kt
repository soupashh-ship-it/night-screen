package com.example.nightscreen.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** Meaningful haptic moments. Kept sparse on purpose. */
enum class HapticKind {
    /** Filter turned on/off. */
    ACTIVATE,

    /** Filter paused/resumed. */
    TOGGLE,

    /** A preset or colour swatch was selected. */
    SELECT,

    /** Slider crossed a meaningful step (e.g. every 5%). */
    STEP,

    /** The action could not be performed. */
    INVALID
}

/**
 * Wraps platform haptics behind the user's "Haptics" preference.
 * No-ops when haptics are disabled or no feedback handle is available.
 */
class HapticController(
    private val feedback: HapticFeedback?,
    private val enabled: Boolean
) {
    fun perform(kind: HapticKind) {
        if (!enabled) return
        val f = feedback ?: return
        when (kind) {
            HapticKind.ACTIVATE -> f.performHapticFeedback(HapticFeedbackType.LongPress)
            HapticKind.TOGGLE -> f.performHapticFeedback(HapticFeedbackType.LongPress)
            HapticKind.SELECT -> f.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            HapticKind.STEP -> f.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            HapticKind.INVALID -> f.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

val LocalHaptics = staticCompositionLocalOf { HapticController(null, false) }

/** Provides the haptic controller for the subtree with the given preference. */
@Composable
fun ProvideHaptics(enabled: Boolean, content: @Composable () -> Unit) {
    val feedback = LocalHapticFeedback.current
    CompositionLocalProvider(
        LocalHaptics provides HapticController(feedback, enabled),
        content = content
    )
}
