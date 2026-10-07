package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.StopwatchState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandViolet
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StopwatchRing(
    elapsedMillis: Long,
    state: StopwatchState,
    modifier: Modifier = Modifier,
    size: Dp = 300.dp,
    content: @Composable () -> Unit
) {
    val isRunning = state == StopwatchState.RUNNING
    val isDark = AppTheme.isDark
    val trackBgColor = AppTheme.ringTrack

    val infiniteTransition = rememberInfiniteTransition(label = "ring_transition")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse animation for glow
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Exact fraction of current second (0.0 .. 1.0) for the 60-second progress arc
    val secondsFraction = ((elapsedMillis % 60000L) / 60000f).coerceIn(0f, 1f)
    val sweepAngle = secondsFraction * 360f

    val ringBrush = Brush.sweepGradient(
        colors = listOf(
            BrandPurple,
            BrandCyan,
            BrandEmerald,
            BrandPink,
            BrandViolet,
            BrandPurple
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (canvasSize / 2f) - 18.dp.toPx()
            val strokeWidth = 5.dp.toPx()
            val arcSize = Size(radius * 2, radius * 2)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)

            // 1. Background static track
            drawCircle(
                color = trackBgColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // 2. Dial tick marks around perimeter (60 ticks for seconds)
            val tickCount = 60
            for (i in 0 until tickCount) {
                val tickAngle = (i * (360f / tickCount)) * (PI / 180f).toFloat()
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                val tickStroke = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                val tickColor = if (isMajor) {
                    if (isRunning) BrandCyan.copy(alpha = 0.85f) else (if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF475569))
                } else {
                    if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFF0F172A).copy(alpha = 0.15f)
                }

                val innerR = radius - 10.dp.toPx()
                val outerR = innerR - tickLength

                val startX = center.x + innerR * cos(tickAngle)
                val startY = center.y + innerR * sin(tickAngle)
                val endX = center.x + outerR * cos(tickAngle)
                val endY = center.y + outerR * sin(tickAngle)

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )
            }

            // 3. Dynamic Progress Ring
            if (isRunning) {
                // Outer rotating ambient glow
                rotate(degrees = rotationAngle, pivot = center) {
                    drawArc(
                        brush = ringBrush,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round),
                        alpha = pulseAlpha * 0.75f
                    )
                }

                // Orbiting comet head dot
                val particleAngleRad = ((sweepAngle - 90f) * (PI / 180f)).toFloat()
                val particleX = center.x + radius * cos(particleAngleRad)
                val particleY = center.y + radius * sin(particleAngleRad)

                // Particle outer halo
                drawCircle(
                    color = BrandEmerald.copy(alpha = 0.45f),
                    radius = 12.dp.toPx(),
                    center = Offset(particleX, particleY)
                )

                // Particle head
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(particleX, particleY)
                )
            } else if (state == StopwatchState.PAUSED) {
                // Static amber paused arc
                drawArc(
                    color = Color(0xFFFFB800),
                    startAngle = -90f,
                    sweepAngle = if (sweepAngle > 0f) sweepAngle else 45f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Timer content inside
        content()
    }
}
