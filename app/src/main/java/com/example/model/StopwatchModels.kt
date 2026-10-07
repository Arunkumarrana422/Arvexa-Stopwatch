package com.example.model

enum class StopwatchState {
    IDLE,
    RUNNING,
    PAUSED
}

data class Lap(
    val id: Long = System.currentTimeMillis(),
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long,
    val diffFromPreviousMillis: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

enum class PrecisionMode(val label: String, val pattern: String) {
    SECONDS("1.0 s", "s"),
    CENTISECONDS("0.01 s", "cs"),
    MILLISECONDS("0.001 s", "ms")
}

enum class VolumeAction(val label: String) {
    START_PAUSE("Start / Pause"),
    LAP("Record Lap"),
    STOP("Stop / Save"),
    RESET("Reset Timer"),
    NONE("No Action")
}

enum class ThemeMode(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("Auto")
}

data class StopwatchSettings(
    val autoStartOnLaunch: Boolean = false,
    val keepScreenAwake: Boolean = true,
    val precision: PrecisionMode = PrecisionMode.CENTISECONDS,
    val volumeControlEnabled: Boolean = true,
    val volumeUpAction: VolumeAction = VolumeAction.START_PAUSE,
    val volumeDownAction: VolumeAction = VolumeAction.LAP,
    val showOngoingNotification: Boolean = true,
    val showLapInNotification: Boolean = true,
    val soundCuesEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val animationsEnabled: Boolean = true
)

enum class AppScreen(val title: String) {
    STOPWATCH("Stopwatch"),
    LAPS("Laps"),
    STATS("Stats"),
    SETTINGS("Settings")
}
