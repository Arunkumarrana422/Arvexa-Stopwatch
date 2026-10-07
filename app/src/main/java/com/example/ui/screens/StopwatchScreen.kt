package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppScreen
import com.example.model.PrecisionMode
import com.example.model.StopwatchState
import com.example.model.ThemeMode
import com.example.service.StopwatchManager
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.LargeStartPauseButton
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.StopwatchRing
import com.example.ui.components.ThemePillToggle
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.GoldBestLap
import com.example.ui.theme.StatusPaused
import com.example.ui.theme.StatusRunning
import com.example.util.TimeFormatter

@Composable
fun StopwatchScreen(
    stopwatchManager: StopwatchManager,
    precisionMode: PrecisionMode,
    themeMode: ThemeMode,
    onThemeChanged: (ThemeMode) -> Unit,
    onNavigateTo: (AppScreen) -> Unit,
    onLockScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by stopwatchManager.state.collectAsState()
    val elapsedMillis by stopwatchManager.elapsedMillis.collectAsState()
    val currentLapMillis by stopwatchManager.currentLapMillis.collectAsState()
    val laps by stopwatchManager.laps.collectAsState()
    val bestLap by stopwatchManager.bestLap.collectAsState()
    val avgLapMillis by stopwatchManager.avgLapMillis.collectAsState()

    val isDark = AppTheme.isDark
    val timeParts = TimeFormatter.getTimeParts(elapsedMillis, precisionMode)
    val lapParts = TimeFormatter.getTimeParts(currentLapMillis, precisionMode)

    var showResetDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_status")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_dot_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // TOP BAR
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            // App Logo + Title (perfectly vertically aligned with logo height)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Arvexa Stopwatch Icon",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = "ARVEXA",
                        color = AppTheme.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "PRO STOPWATCH",
                        color = BrandCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }

            // Quick Theme Pill Switch + Settings Shortcut
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pill-shaped Dark Mode toggle button
                ThemePillToggle(
                    currentTheme = themeMode,
                    onThemeChanged = onThemeChanged
                )

                GlassIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Settings",
                    onClick = { onNavigateTo(AppScreen.SETTINGS) },
                    size = 38.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live Status Badge
        val statusColor = when (state) {
            StopwatchState.RUNNING -> StatusRunning
            StopwatchState.PAUSED -> StatusPaused
            StopwatchState.IDLE -> if (isDark) Color(0xFF6B7280) else Color(0xFF94A3B8)
        }
        val statusText = when (state) {
            StopwatchState.RUNNING -> "RUNNING"
            StopwatchState.PAUSED -> "PAUSED"
            StopwatchState.IDLE -> "READY"
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0xFF131B2E) else Color(0xFFFFFFFF))
                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(if (state == StopwatchState.RUNNING) pulseScale else 1f)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CENTER STOPWATCH RING & MAIN TIMER
        StopwatchRing(
            elapsedMillis = elapsedMillis,
            state = state,
            size = 280.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Status Subheading
                Text(
                    text = when (state) {
                        StopwatchState.RUNNING -> "TIME ELAPSED"
                        StopwatchState.PAUSED -> "TIMER PAUSED"
                        StopwatchState.IDLE -> "TAP START"
                    },
                    color = AppTheme.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Giant Digital Timer
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeParts.mainDisplay,
                        color = AppTheme.textPrimary,
                        fontSize = if (timeParts.hours.toInt() > 0) 36.sp else 46.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    if (timeParts.fractionDisplay.isNotEmpty()) {
                        Text(
                            text = timeParts.fractionDisplay,
                            color = BrandCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Current Lap Live Sub-ticker
                val lapNumber = laps.size + 1
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF111A2D) else Color(0xFFE9EEF7))
                        .border(1.dp, Color(0x3300C2FF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LAP %02d".format(lapNumber),
                            color = BrandEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${lapParts.mainDisplay}${lapParts.fractionDisplay}",
                            color = AppTheme.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // QUICK STATS CHIPS (Best Lap & Avg Lap)
        AnimatedVisibility(
            visible = laps.isNotEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Best Lap Chip (Rounded Ripple Clickable)
                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 14.dp,
                    onClick = { onNavigateTo(AppScreen.LAPS) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "BEST LAP",
                                color = GoldBestLap,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bestLap?.let { TimeFormatter.format(it.lapTimeMillis, precisionMode) } ?: "--:--",
                                color = AppTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Best Lap",
                            tint = GoldBestLap,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Total Laps Chip (Rounded Ripple Clickable)
                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 14.dp,
                    onClick = { onNavigateTo(AppScreen.LAPS) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "AVG SPLIT",
                                color = BrandCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (avgLapMillis > 0) TimeFormatter.format(avgLapMillis, precisionMode) else "--:--",
                                color = AppTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Total Laps",
                            tint = BrandCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ACTION BUTTONS SECTION (Large Icon-Only Center Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (state) {
                StopwatchState.IDLE -> {
                    // Large centered START button (Icon only)
                    LargeStartPauseButton(
                        state = state,
                        onClick = { stopwatchManager.start() }
                    )
                }

                StopwatchState.RUNNING -> {
                    // LAP Button
                    SecondaryActionButton(
                        text = "LAP",
                        icon = Icons.Default.Flag,
                        gradient = Brush.horizontalGradient(listOf(BrandPurple, BrandCyan)),
                        onClick = { stopwatchManager.recordLap() },
                        testTag = "action_lap_button"
                    )

                    // PAUSE Button (Icon only)
                    LargeStartPauseButton(
                        state = state,
                        onClick = { stopwatchManager.pause() }
                    )

                    // LOCK Screen Button
                    SecondaryActionButton(
                        text = "LOCK",
                        icon = Icons.Default.Lock,
                        gradient = Brush.horizontalGradient(listOf(BrandCyan, BrandPurple)),
                        onClick = onLockScreen,
                        testTag = "action_lock_button"
                    )
                }

                StopwatchState.PAUSED -> {
                    // RESET Button with Confirmation Dialog
                    SecondaryActionButton(
                        text = "RESET",
                        icon = Icons.Default.Refresh,
                        gradient = Brush.horizontalGradient(
                            if (isDark) listOf(Color(0xFF6B7280), Color(0xFF4B5563))
                            else listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                        ),
                        onClick = {
                            if (elapsedMillis > 0 || laps.isNotEmpty()) {
                                showResetDialog = true
                            } else {
                                stopwatchManager.reset()
                            }
                        },
                        testTag = "action_reset_button"
                    )

                    // RESUME Button (Icon only)
                    LargeStartPauseButton(
                        state = state,
                        onClick = { stopwatchManager.resume() }
                    )

                    // STOP & SAVE Button
                    SecondaryActionButton(
                        text = "SAVE",
                        icon = Icons.Default.Stop,
                        gradient = Brush.horizontalGradient(listOf(BrandEmerald, BrandCyan)),
                        onClick = { stopwatchManager.stopAndSave() },
                        testTag = "action_save_button"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RECENT LAPS QUICK GLANCE (SHOWS 4 LAPS, ROUNDED RIPPLE EFFECT)
        if (laps.isNotEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                onClick = { onNavigateTo(AppScreen.LAPS) }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT LAPS (${laps.size})",
                            color = AppTheme.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "View All",
                            color = BrandCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Exactly 4 recent laps displayed
                    val latestLaps = laps.take(4)
                    latestLaps.forEach { lap ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(
                                            if (isDark) Color(0xFF182238) else Color(0xFFE2E8F0),
                                            CircleShape
                                        )
                                ) {
                                    Text(
                                        text = "#${lap.lapNumber}",
                                        color = AppTheme.textPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = TimeFormatter.format(lap.lapTimeMillis, precisionMode),
                                    color = AppTheme.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (lap.diffFromPreviousMillis != 0L) {
                                val isFaster = lap.diffFromPreviousMillis < 0
                                Text(
                                    text = TimeFormatter.formatLapDifference(lap.diffFromPreviousMillis),
                                    color = if (isFaster) BrandEmerald else BrandPink,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Resetting Stopwatch
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = if (isDark) Color(0xFF131D33) else Color(0xFFFFFFFF),
            titleContentColor = AppTheme.textPrimary,
            textContentColor = AppTheme.textSecondary,
            title = {
                Text("Reset Stopwatch?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to reset the stopwatch session? All elapsed time and recorded laps will be cleared.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        stopwatchManager.reset()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
                ) {
                    Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = AppTheme.textSecondary)
                }
            }
        )
    }
}
