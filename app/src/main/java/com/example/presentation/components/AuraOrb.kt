package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.VoiceState
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraViolet
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AuraOrb(
    voiceState: VoiceState,
    audioLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuraOrbAnimations")

    // Idle breathing pulse
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingScale"
    )

    // Continuous rotation angle for glowing gradient
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Wave phase animation for active speech
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    val isListening = voiceState is VoiceState.Listening
    val isProcessing = voiceState is VoiceState.Processing
    val isExecuting = voiceState is VoiceState.Executing
    val isError = voiceState is VoiceState.Error

    val activeScale = when {
        isListening -> 1f + (audioLevel * 0.35f)
        isProcessing -> 1.08f
        isExecuting -> 1.12f
        else -> breathingScale
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("aura_orb_button")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.72f * activeScale

            // 1. Outermost Ambient Glow Aura
            val glowColor = when {
                isError -> AuraError.copy(alpha = 0.25f)
                isListening -> AuraCyan.copy(alpha = 0.35f + (audioLevel * 0.35f))
                isProcessing -> AuraViolet.copy(alpha = 0.4f)
                else -> AuraCyan.copy(alpha = 0.20f)
            }
            drawCircle(
                color = glowColor,
                radius = baseRadius * 1.35f,
                center = center
            )

            // 2. Orbital Energy Wave Rings (when Listening or Processing)
            if (isListening || isProcessing) {
                val ringCount = 3
                for (i in 1..ringCount) {
                    val ringRadius = baseRadius + (i * 12f * (if (isListening) (1f + audioLevel) else 1f))
                    val ringAlpha = (0.35f / i)
                    drawCircle(
                        color = if (i % 2 == 0) AuraViolet.copy(alpha = ringAlpha) else AuraCyan.copy(alpha = ringAlpha),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            // 3. Core Gradient Sphere
            val primaryOrbColor = when {
                isError -> AuraError
                else -> AuraCyan
            }
            val secondaryOrbColor = when {
                isError -> Color(0xFFFF8A80)
                else -> AuraViolet
            }
            val tertiaryOrbColor = when {
                isError -> Color(0xFFD50000)
                else -> AuraMagenta
            }

            val dynamicBrush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.9f),
                    primaryOrbColor,
                    secondaryOrbColor,
                    tertiaryOrbColor,
                    Color(0xFF0F172A)
                ),
                center = center,
                radius = baseRadius
            )

            drawCircle(
                brush = dynamicBrush,
                radius = baseRadius,
                center = center
            )

            // 4. Center Audio Waveform (Oscilloscope effect)
            if (isListening) {
                val wavePath = Path()
                val waveWidth = baseRadius * 1.4f
                val startX = center.x - (waveWidth / 2f)
                val points = 30
                val step = waveWidth / points
                val amplitude = (baseRadius * 0.28f) * (0.2f + audioLevel * 0.8f)

                for (j in 0..points) {
                    val x = startX + (j * step)
                    val normalizedX = (j.toFloat() / points) * 2 * PI.toFloat()
                    val y = center.y + (sin(normalizedX * 2f + wavePhase) * amplitude)
                    if (j == 0) {
                        wavePath.moveTo(x, y)
                    } else {
                        wavePath.lineTo(x, y)
                    }
                }

                drawPath(
                    path = wavePath,
                    color = Color.White.copy(alpha = 0.95f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            } else if (isProcessing) {
                // Spinning orbital dot
                val orbitRadius = baseRadius * 0.55f
                val angleRad = Math.toRadians(rotationAngle.toDouble())
                val dotX = center.x + (orbitRadius * kotlin.math.cos(angleRad)).toFloat()
                val dotY = center.y + (orbitRadius * kotlin.math.sin(angleRad)).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}
