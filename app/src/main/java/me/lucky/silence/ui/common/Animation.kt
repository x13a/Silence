package me.lucky.silence.ui.common

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

object Animation {
    private const val FADE_OUT_MILLIS = 75
    private const val FADE_IN_MILLIS = 300
    val slideInEffect = tween<IntOffset>(
        durationMillis = FADE_IN_MILLIS,
        delayMillis = FADE_OUT_MILLIS,
        easing = LinearOutSlowInEasing,
    )
    val slideOutEffect = tween<IntOffset>(durationMillis = FADE_IN_MILLIS)
    val fadeOutEffect = tween<Float>(
        durationMillis = FADE_OUT_MILLIS,
        easing = FastOutLinearInEasing,
    )
    val fadeInEffect = tween<Float>(
        durationMillis = FADE_IN_MILLIS,
        delayMillis = FADE_OUT_MILLIS,
        easing = LinearOutSlowInEasing,
    )
    val offsetFunc: (offsetForFullSlide: Int) -> Int = { it.div(5) }
}