package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.model.PrecisionMode
import com.example.model.WorkoutSession
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPink
import com.example.util.LapPdfGenerator
import com.example.util.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AllWorkoutsScreen(
    precisionMode: PrecisionMode,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle system back button to return to Stats screen
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = AppDatabase.getInstance(context)
    val isDark = AppTheme.isDark
    val rawSessions by database.workoutSessionDao().getAllSessions().collectAsState(initial = emptyList())

    // Ensure sessions are sorted newest first
    val sessions = remember(rawSessions) {
        rawSessions.sortedByDescending { it.timestamp }
    }

    var showClearAllDialog by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<WorkoutSession?>(null) }

    val totalTimeMillis = sessions.sumOf { it.durationMillis }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .testTag("all_workouts_screen")
        ) {
        // TOP APP BAR / HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Stats",
                    onClick = onBack,
                    size = 40.dp,
                    modifier = Modifier.testTag("btn_back_all_workouts")
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "WORKOUTS",
                        color = AppTheme.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${sessions.size} recorded sessions",
                        color = AppTheme.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (sessions.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share Workout History PDF",
                        onClick = {
                            LapPdfGenerator.generateAndShareWorkoutHistoryReport(
                                context = context,
                                sessions = sessions,
                                precisionMode = precisionMode
                            )
                        },
                        size = 38.dp,
                        modifier = Modifier.testTag("btn_share_all_workouts_pdf")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showClearAllDialog = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("btn_clear_all_workouts")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All",
                            tint = BrandPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear All",
                            color = BrandPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // STATS SUMMARY PILLS (if sessions exist)
        if (sessions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Count Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2200E5FF))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = BrandCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "TOTAL WORKOUTS",
                                color = AppTheme.textSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sessions.size}",
                                color = AppTheme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Total Duration Badge
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2210B981))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = BrandEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "TOTAL TIME",
                                color = AppTheme.textSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = TimeFormatter.format(totalTimeMillis, precisionMode),
                                color = BrandEmerald,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LIST OF ALL WORKOUT SESSIONS (Newest on top)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (sessions.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "No sessions",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Saved Workouts Yet",
                                color = AppTheme.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When you stop a stopwatch session, your workout history will be listed here with newest on top.",
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
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
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
                val displayTitle = if (session.title.startsWith("Run Session") || session.title.isBlank()) "Running Session" else session.title
                Text("Delete \"$displayTitle\" (${TimeFormatter.formatDate(session.timestamp)})? This cannot be undone.")
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
