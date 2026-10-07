package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lap
import com.example.model.PrecisionMode
import com.example.service.StopwatchManager
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.GoldBestLap
import com.example.util.TimeFormatter

@Composable
fun LapsScreen(
    stopwatchManager: StopwatchManager,
    precisionMode: PrecisionMode,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = AppTheme.isDark
    val laps by stopwatchManager.laps.collectAsState()
    val bestLap by stopwatchManager.bestLap.collectAsState()
    val slowestLap by stopwatchManager.slowestLap.collectAsState()
    val avgLapMillis by stopwatchManager.avgLapMillis.collectAsState()
    val elapsedMillis by stopwatchManager.elapsedMillis.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // TOP HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            Column {
                Text(
                    text = "LAP HISTORY",
                    color = AppTheme.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "${laps.size} Total Laps Recorded",
                    color = BrandCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Top action buttons: Share & Clear
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (laps.isNotEmpty()) {
                    GlassIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share Laps",
                        onClick = {
                            val shareText = buildShareReport(laps, elapsedMillis, bestLap, avgLapMillis, precisionMode)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share RunStop Lap Results")
                            context.startActivity(shareIntent)
                        },
                        size = 40.dp
                    )

                    GlassIconButton(
                        icon = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Laps",
                        tint = BrandPink,
                        onClick = { showClearDialog = true },
                        size = 40.dp
                    )
                }
            }
        }

        // AGGREGATE STATS BANNER
        if (laps.isNotEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                cornerRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Best Lap
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Best",
                                tint = GoldBestLap,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BEST", color = GoldBestLap, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bestLap?.let { TimeFormatter.format(it.lapTimeMillis, precisionMode) } ?: "--:--",
                            color = AppTheme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = bestLap?.let { "Lap ${it.lapNumber}" } ?: "",
                            color = AppTheme.textSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Average Lap
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Avg",
                                tint = BrandCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AVERAGE", color = BrandCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (avgLapMillis > 0) TimeFormatter.format(avgLapMillis, precisionMode) else "--:--",
                            color = AppTheme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${laps.size} splits",
                            color = AppTheme.textSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Slowest Lap
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Slowest",
                                tint = BrandPink,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SLOWEST", color = BrandPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slowestLap?.let { TimeFormatter.format(it.lapTimeMillis, precisionMode) } ?: "--:--",
                            color = AppTheme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = slowestLap?.let { "Lap ${it.lapNumber}" } ?: "",
                            color = AppTheme.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // LAPS LIST OR EMPTY STATE
        if (laps.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                if (isDark) Color(0xFF131B2F) else Color(0xFFE2E8F0),
                                CircleShape
                            )
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "No Laps",
                            tint = BrandCyan.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Laps Recorded",
                        color = AppTheme.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Start the timer and tap LAP to track your splits.",
                        color = AppTheme.textSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(laps, key = { it.id }) { lap ->
                    val isBest = lap.id == bestLap?.id && laps.size > 1
                    val isSlowest = lap.id == slowestLap?.id && laps.size > 1

                    LapCard(
                        lap = lap,
                        isBest = isBest,
                        isSlowest = isSlowest,
                        precisionMode = precisionMode
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Clearing Laps
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = if (isDark) Color(0xFF131D33) else Color(0xFFFFFFFF),
            titleContentColor = AppTheme.textPrimary,
            textContentColor = AppTheme.textSecondary,
            title = { Text("Clear All Laps?") },
            text = { Text("Are you sure you want to clear all ${laps.size} recorded laps for this session? The total timer will continue running.") },
            confirmButton = {
                Button(
                    onClick = {
                        stopwatchManager.clearLaps()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
                ) {
                    Text("Clear", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = AppTheme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun LapCard(
    lap: Lap,
    isBest: Boolean,
    isSlowest: Boolean,
    precisionMode: PrecisionMode,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val cardBg = when {
        isBest -> if (isDark) Color(0xFF0F2624) else Color(0xFFE6F9F5)
        isSlowest -> if (isDark) Color(0xFF261220) else Color(0xFFFFEEF3)
        else -> AppTheme.cardBackground
    }

    val borderBrush = when {
        isBest -> Brush.horizontalGradient(listOf(BrandEmerald, BrandCyan))
        isSlowest -> Brush.horizontalGradient(listOf(BrandPink, BrandPurple))
        else -> if (isDark) {
            Brush.linearGradient(listOf(Color(0xFF1E2A44), Color(0xFF1E2A44)))
        } else {
            Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1)))
        }
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        borderBrush = borderBrush,
        backgroundColor = cardBg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left: Lap Number & Status Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            brush = when {
                                isBest -> Brush.linearGradient(listOf(BrandEmerald, BrandCyan))
                                isSlowest -> Brush.linearGradient(listOf(BrandPink, BrandPurple))
                                else -> if (isDark) {
                                    Brush.linearGradient(listOf(Color(0xFF19233A), Color(0xFF19233A)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0)))
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Text(
                        text = "%02d".format(lap.lapNumber),
                        color = if (isBest || isSlowest || isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LAP ${lap.lapNumber}",
                            color = AppTheme.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isBest) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandEmerald)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "BEST",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        } else if (isSlowest) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandPink)
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SLOWEST",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Total: ${TimeFormatter.format(lap.totalTimeMillis, precisionMode)}",
                        color = AppTheme.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Right: Split Time & Delta Difference
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = TimeFormatter.format(lap.lapTimeMillis, precisionMode),
                    color = if (isBest) BrandEmerald else AppTheme.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

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

private fun buildShareReport(
    laps: List<Lap>,
    totalMillis: Long,
    bestLap: Lap?,
    avgLapMillis: Long,
    precisionMode: PrecisionMode
): String {
    val sb = StringBuilder()
    sb.append("RUNSTOP • Stopwatch Lap Report\n")
    sb.append("════════════════════════════════\n")
    sb.append("Total Time: ${TimeFormatter.format(totalMillis, precisionMode)}\n")
    sb.append("Total Laps: ${laps.size}\n")
    if (bestLap != null) {
        sb.append("Best Lap: Lap ${bestLap.lapNumber} (${TimeFormatter.format(bestLap.lapTimeMillis, precisionMode)})\n")
    }
    if (avgLapMillis > 0) {
        sb.append("Average Lap: ${TimeFormatter.format(avgLapMillis, precisionMode)}\n")
    }
    sb.append("\nLAP SPLITS:\n")
    for (lap in laps.reversed()) {
        val diff = if (lap.diffFromPreviousMillis != 0L) " (${TimeFormatter.formatLapDifference(lap.diffFromPreviousMillis)})" else ""
        sb.append("Lap ${lap.lapNumber}: ${TimeFormatter.format(lap.lapTimeMillis, precisionMode)}$diff • Total: ${TimeFormatter.format(lap.totalTimeMillis, precisionMode)}\n")
    }
    sb.append("\nTracked with RunStop – Runner Stopwatch")
    return sb.toString()
}
