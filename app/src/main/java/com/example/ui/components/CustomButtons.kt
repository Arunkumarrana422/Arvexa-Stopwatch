package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StopwatchState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.ButtonPauseGradient
import com.example.ui.theme.ButtonStartGradient

@Composable
fun LargeStartPauseButton(
    state: StopwatchState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "button_press_scale"
    )

    // Glowing pulsation when running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val isRunning = state == StopwatchState.RUNNING
    val isPaused = state == StopwatchState.PAUSED
    val isIdle = state == StopwatchState.IDLE

    val gradient = when {
        isRunning -> ButtonPauseGradient
        isPaused -> Brush.horizontalGradient(listOf(BrandCyan, BrandEmerald))
        else -> ButtonStartGradient
    }

    val glowColor = when {
        isRunning -> BrandPink
        isPaused -> BrandCyan
        else -> BrandEmerald
    }

    val icon = when {
        isRunning -> Icons.Default.Pause
        else -> Icons.Default.PlayArrow
    }

    val iconDescription = when {
        isRunning -> "Pause"
        isPaused -> "Resume"
        else -> "Start"
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(108.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
            .testTag("main_start_pause_button")
    ) {
        // Outer ambient glow ring
        Box(
            modifier = Modifier
                .size(108.dp)
                .background(
                    color = glowColor.copy(alpha = if (isRunning) glowAlpha * 0.45f else 0.22f),
                    shape = CircleShape
                )
        )

        // Main inner circular button with ONLY THE ICON
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .shadow(
                    elevation = if (isRunning) 18.dp else 12.dp,
                    shape = CircleShape,
                    spotColor = glowColor
                )
                .background(brush = gradient, shape = CircleShape)
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = Color.White,
                modifier = Modifier
                    .size(if (isRunning) 40.dp else 44.dp)
                    // Slight optical centering offset for play arrow
                    .offset(x = if (!isRunning) 2.dp else 0.dp)
            )
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    enabled: Boolean = true,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val isDark = AppTheme.isDark

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "sec_button_scale"
    )

    val bgColor = if (isDark) {
        if (enabled) Color(0xFF141D30) else Color(0xFF0F1524)
    } else {
        if (enabled) Color(0xFFFFFFFF) else Color(0xFFE2E8F0)
    }

    val textColor = if (isDark) {
        if (enabled) Color.White else Color.White.copy(alpha = 0.35f)
    } else {
        if (enabled) Color(0xFF0F172A) else Color(0xFF94A3B8)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 96.dp, height = 54.dp)
            .scale(scale)
            .shadow(if (!isDark && enabled) 4.dp else 0.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor, RoundedCornerShape(28.dp))
            .border(
                width = 1.5.dp,
                brush = if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFF8E9BAE), Color(0xFF8E9BAE))),
                shape = RoundedCornerShape(28.dp)
            )
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                            onClick()
                        }
                    )
                }
            }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (enabled) (if (isDark) Color.White else Color(0xFF0F172A)) else textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val isDark = AppTheme.isDark
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "icon_scale")

    val finalTint = tint ?: if (isDark) Color.White else Color(0xFF0F172A)
    val bgColor = if (isDark) Color(0x20FFFFFF) else Color(0x33000000)
    val borderColor = if (isDark) Color(0x30FFFFFF) else Color(0x22000000)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = finalTint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
