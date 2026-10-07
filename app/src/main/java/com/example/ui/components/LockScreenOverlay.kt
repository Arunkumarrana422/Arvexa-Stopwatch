package com.example.ui.components

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrecisionMode
import com.example.service.StopwatchManager
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import com.example.util.HapticHelper
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun LockScreenOverlay(
    onUnlock: () -> Unit,
    stopwatchManager: StopwatchManager,
    precisionMode: PrecisionMode,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val elapsedMillis by stopwatchManager.elapsedMillis.collectAsState()
    val timeParts = TimeFormatter.getTimeParts(elapsedMillis, precisionMode)

    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    // Intercept back button when locked
    BackHandler(enabled = true) {
        HapticHelper.trigger(context, true, HapticHelper.HapticType.LIGHT)
    }

    // Handle 5-second (5000ms) continuous press
    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = SystemClock.elapsedRealtime()
            val totalDuration = 5000L
            var lastSecondTick = 0

            while (isActive && isHolding) {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                holdProgress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)

                // Haptic pulse on each second of hold
                val currentSecond = (elapsed / 1000).toInt()
                if (currentSecond > lastSecondTick && currentSecond < 5) {
                    lastSecondTick = currentSecond
                    HapticHelper.trigger(context, true, HapticHelper.HapticType.LIGHT)
                }

                if (holdProgress >= 1f) {
                    HapticHelper.trigger(context, true, HapticHelper.HapticType.SUCCESS)
                    onUnlock()
                    break
                }
                delay(16) // Smooth 60 FPS update
            }
        } else {
            holdProgress = 0f
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lock_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lock_glow_alpha"
    )

    // Fullscreen dark translucent veil that consumes all touch input
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6050812)) // Semi-transparent black veil
            .pointerInput(Unit) {
                // Completely absorb any clicks so nothing behind can be clicked
                detectTapGestures { }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp, horizontal = 24.dp)
        ) {
            // TOP SECTION: Status Badge & Hardware Volume Indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x3300C2FF))
                        .border(1.dp, BrandCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Screen Locked",
                            tint = BrandCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCREEN LOCKED",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume Controls Active",
                        tint = BrandEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Physical Volume Keys Active",
                        color = BrandEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Faint live timer readout
                Text(
                    text = "${timeParts.mainDisplay}${timeParts.fractionDisplay}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            // CENTER SECTION: 5-Second Hold-to-Unlock Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(130.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    HapticHelper.trigger(context, true, HapticHelper.HapticType.LIGHT)
                                    isHolding = true
                                    tryAwaitRelease()
                                    isHolding = false
                                }
                            )
                        }
                ) {
                    // Outer Canvas with circular progress arc
                    Canvas(modifier = Modifier.size(124.dp)) {
                        val strokeWidthPx = 6.dp.toPx()
                        val arcPadding = strokeWidthPx / 2f
                        val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                        val arcOffset = Offset(arcPadding, arcPadding)

                        // Inactive track ring
                        drawArc(
                            color = Color(0x33FFFFFF),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )

                        // Active progress arc
                        if (holdProgress > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(BrandCyan, BrandPurple, BrandEmerald, BrandCyan)
                                ),
                                startAngle = -90f,
                                sweepAngle = holdProgress * 360f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Center Circular Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(
                                elevation = if (isHolding) 16.dp else 8.dp,
                                shape = CircleShape,
                                spotColor = BrandCyan
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isHolding) listOf(BrandCyan, BrandPurple)
                                    else listOf(Color(0xFF162138), Color(0xFF0F1829))
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isHolding) Color.White else Color(0x4400C2FF),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (holdProgress >= 0.95f) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = "Hold to unlock",
                            tint = if (isHolding) Color.White else BrandCyan.copy(alpha = pulseAlpha),
                            modifier = Modifier
                                .size(42.dp)
                                .scale(if (isHolding) 1.1f else 1.0f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Countdown / Instruction Label
                if (isHolding) {
                    val secondsLeft = ((1f - holdProgress) * 5f + 0.95f).toInt().coerceAtLeast(1)
                    Text(
                        text = "Keep holding: ${secondsLeft}s...",
                        color = BrandCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Text(
                        text = "Hold for 5s to unlock",
                        color = Color(0xFFA7B0C0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Prevents accidental touches during runs",
                    color = Color(0x88FFFFFF),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            // BOTTOM SPACER
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
