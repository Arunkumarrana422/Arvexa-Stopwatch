package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.model.PrecisionMode
import com.example.model.WorkoutSession
import com.example.service.StopwatchManager
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandViolet
import com.example.ui.theme.GoldBestLap
import com.example.util.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(
    stopwatchManager: StopwatchManager,
    precisionMode: PrecisionMode,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getInstance(context)
    val isDark = AppTheme.isDark
    val sessions by database.workoutSessionDao().getAllSessions().collectAsState(initial = emptyList())
    val activeLaps by stopwatchManager.laps.collectAsState()

    var showClearAllDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<WorkoutSession?>(null) }

    // Calculated overall stats
    val totalSessions = sessions.size
    val totalTimeMillis = sessions.sumOf { it.durationMillis }
    val totalLaps = sessions.sumOf { it.lapCount }
    val bestOverallLap = sessions.filter { it.bestLapMillis > 0 }.minOfOrNull { it.bestLapMillis } ?: 0L
    val longestSession = sessions.maxOfOrNull { it.durationMillis } ?: 0L
    val avgSessionDuration = if (totalSessions > 0) totalTimeMillis / totalSessions else 0L

    LazyColumn(
        contentPadding = PaddingValues(bottom = 90.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // HEADER
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                Text(
                    text = "RUNNING STATS",
                    color = AppTheme.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Performance analytics & workout logs",
                    color = BrandCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // TOP METRICS GRID (2 columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Total Sessions",
                        value = "$totalSessions",
                        icon = Icons.Default.DirectionsRun,
                        iconColor = BrandCyan,
                        modifier = Modifier.weight(1f),
                        subtitle = "Saved workouts"
                    )
                    StatMetricCard(
                        title = "Total Time",
                        value = TimeFormatter.formatDurationShort(totalTimeMillis),
                        icon = Icons.Default.Timer,
                        iconColor = BrandEmerald,
                        modifier = Modifier.weight(1f),
                        subtitle = "Active running"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Total Laps",
                        value = "$totalLaps",
                        icon = Icons.Default.Flag,
                        iconColor = BrandPurple,
                        modifier = Modifier.weight(1f),
                        subtitle = "Splits recorded"
                    )
                    StatMetricCard(
                        title = "Best Lap Ever",
                        value = if (bestOverallLap > 0) TimeFormatter.format(bestOverallLap, precisionMode) else "--:--",
                        icon = Icons.Default.EmojiEvents,
                        iconColor = GoldBestLap,
                        modifier = Modifier.weight(1f),
                        subtitle = "All-time PR"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Longest Session",
                        value = if (longestSession > 0) TimeFormatter.formatDurationShort(longestSession) else "--:--",
                        icon = Icons.Default.HourglassTop,
                        iconColor = BrandPink,
                        modifier = Modifier.weight(1f),
                        subtitle = "Single run record"
                    )
                    StatMetricCard(
                        title = "Avg Duration",
                        value = if (avgSessionDuration > 0) TimeFormatter.formatDurationShort(avgSessionDuration) else "--:--",
                        icon = Icons.Default.Speed,
                        iconColor = BrandViolet,
                        modifier = Modifier.weight(1f),
                        subtitle = "Per workout"
                    )
                }
            }
        }

        // PACE CURVE CHART
        item {
            Spacer(modifier = Modifier.height(16.dp))
            PaceCurveChartCard(activeLaps = activeLaps, sessions = sessions)
        }

        // SESSION HISTORY SECTION
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORKOUT HISTORY (${sessions.size})",
                    color = AppTheme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (sessions.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = BrandPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            showClearAllDialog = true
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (sessions.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "No sessions",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Saved Workouts Yet",
                            color = AppTheme.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "When you stop a stopwatch session, it will be saved here automatically.",
                            color = AppTheme.textSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                WorkoutSessionCard(
                    session = session,
                    precisionMode = precisionMode,
                    onDelete = {
                        sessionToDelete = session
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Confirmation Dialog for Clearing All Sessions
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            containerColor = if (isDark) Color(0xFF131D33) else Color(0xFFFFFFFF),
            titleContentColor = AppTheme.textPrimary,
            textContentColor = AppTheme.textSecondary,
            title = {
                Text("Clear All Workouts?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete all saved workout records? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            database.workoutSessionDao().clearAllSessions()
                        }
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = AppTheme.textSecondary)
                }
            }
        )
    }

    // Confirmation Dialog for Deleting Single Workout Session
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            containerColor = if (isDark) Color(0xFF131D33) else Color(0xFFFFFFFF),
            titleContentColor = AppTheme.textPrimary,
            textContentColor = AppTheme.textSecondary,
            title = {
                Text("Delete Workout?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete '${session.title}'? This workout record will be permanently deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            database.workoutSessionDao().deleteSession(session)
                        }
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = AppTheme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun PaceCurveChartCard(
    activeLaps: List<com.example.model.Lap>,
    sessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val dataPoints = if (activeLaps.size >= 2) {
        activeLaps.reversed().map { it.lapTimeMillis.toFloat() / 1000f }
    } else if (sessions.isNotEmpty()) {
        sessions.take(7).reversed().map { it.durationMillis.toFloat() / 1000f }
    } else {
        listOf(45f, 42f, 44f, 39f, 41f, 38f)
    }

    val chartTitle = if (activeLaps.size >= 2) "Current Session Lap Pace (s)" else if (sessions.isNotEmpty()) "Recent Workouts Duration (s)" else "Pace Curve Demo (s)"

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Pace Trend",
                        tint = BrandCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = chartTitle.uppercase(),
                        color = AppTheme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandPurple.copy(alpha = 0.25f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = BrandCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart with Gradient Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (dataPoints.isEmpty()) return@Canvas

                    val maxVal = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)
                    val minVal = (dataPoints.minOrNull() ?: 0f).coerceAtLeast(0f)
                    val range = (maxVal - minVal).coerceAtLeast(1f)

                    val width = size.width
                    val height = size.height - 20.dp.toPx()
                    val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

                    val points = dataPoints.mapIndexed { index, value ->
                        val x = index * stepX
                        val normalized = (value - minVal) / range
                        val y = height - (normalized * (height - 10.dp.toPx())) + 5.dp.toPx()
                        Offset(x, y)
                    }

                    // Background Grid Lines
                    val gridColor = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000)
                    for (i in 0..3) {
                        val y = (height / 3) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build filled area path
                    val fillPath = Path().apply {
                        moveTo(0f, height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(width, height)
                        close()
                    }

                    // Draw gradient fill under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BrandCyan.copy(alpha = 0.35f),
                                BrandPurple.copy(alpha = 0.05f)
                            )
                        )
                    )

                    // Draw stroke line
                    val strokePath = Path().apply {
                        points.forEachIndexed { index, pt ->
                            if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(BrandPurple, BrandCyan, BrandEmerald)
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw glowing dots at vertices
                    points.forEach { pt ->
                        drawCircle(
                            color = BrandCyan,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutSessionCard(
    session: WorkoutSession,
    precisionMode: PrecisionMode,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    color = AppTheme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = TimeFormatter.formatDate(session.timestamp),
                    color = AppTheme.textSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duration Icon + Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Duration",
                            tint = BrandEmerald,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = TimeFormatter.format(session.durationMillis, precisionMode),
                            color = BrandEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Laps Icon + Text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Laps",
                            tint = BrandCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${session.lapCount} Laps",
                            color = BrandCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Best Lap Trophy Icon + Text
                    if (session.bestLapMillis > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Best Lap",
                                tint = GoldBestLap,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Best: ${TimeFormatter.format(session.bestLapMillis, precisionMode)}",
                                color = GoldBestLap,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            GlassIconButton(
                icon = Icons.Default.Delete,
                contentDescription = "Delete session",
                tint = Color(0xFF8899B5),
                onClick = onDelete,
                size = 36.dp
            )
        }
    }
}
