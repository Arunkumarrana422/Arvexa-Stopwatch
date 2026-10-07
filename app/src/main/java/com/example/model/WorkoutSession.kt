package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMillis: Long,
    val lapCount: Int,
    val bestLapMillis: Long,
    val avgLapMillis: Long,
    val slowestLapMillis: Long,
    val lapsData: String = "" // Formatted summary or JSON
)
