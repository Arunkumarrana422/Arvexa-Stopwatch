package com.example.service

import android.content.Context
import android.os.SystemClock
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.model.Lap
import com.example.model.StopwatchState
import com.example.model.WorkoutSession
import com.example.util.HapticHelper
import com.example.util.SoundHelper
import com.example.util.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopwatchManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null

    private val settingsRepo = SettingsRepository.getInstance(appContext)
    private val database = AppDatabase.getInstance(appContext)

    // Stopwatch State
    private val _state = MutableStateFlow(StopwatchState.IDLE)
    val state: StateFlow<StopwatchState> = _state.asStateFlow()

    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    private val _currentLapMillis = MutableStateFlow(0L)
    val currentLapMillis: StateFlow<Long> = _currentLapMillis.asStateFlow()

    private val _laps = MutableStateFlow<List<Lap>>(emptyList())
    val laps: StateFlow<List<Lap>> = _laps.asStateFlow()

    private val _bestLap = MutableStateFlow<Lap?>(null)
    val bestLap: StateFlow<Lap?> = _bestLap.asStateFlow()

    private val _slowestLap = MutableStateFlow<Lap?>(null)
    val slowestLap: StateFlow<Lap?> = _slowestLap.asStateFlow()

    private val _avgLapMillis = MutableStateFlow(0L)
    val avgLapMillis: StateFlow<Long> = _avgLapMillis.asStateFlow()

    // Internal timing variables based on monotonic clock
    private var accumulatedElapsed: Long = 0L
    private var startTimestamp: Long = 0L

    private var lapAccumulated: Long = 0L
    private var lapStartTimestamp: Long = 0L

    private var sessionStartTimeEpoch: Long = 0L

    init {
        restorePersistedSession()
    }

    private fun restorePersistedSession() {
        val persisted = settingsRepo.loadActiveSession()
        if (persisted.state != StopwatchState.IDLE) {
            accumulatedElapsed = persisted.accumulatedElapsed
            lapAccumulated = persisted.lapAccumulated
            sessionStartTimeEpoch = persisted.sessionStartTimeEpoch
            _laps.value = persisted.laps
            recalculateLapStats(persisted.laps)

            if (persisted.state == StopwatchState.RUNNING) {
                // Recover running session seamlessly based on elapsed monotonic / wallclock time
                startTimestamp = SystemClock.elapsedRealtime()
                lapStartTimestamp = SystemClock.elapsedRealtime()
                _state.value = StopwatchState.RUNNING
                startTicker()
                StopwatchService.startOrUpdateService(appContext)
            } else {
                _state.value = StopwatchState.PAUSED
                _elapsedMillis.value = accumulatedElapsed
                _currentLapMillis.value = lapAccumulated
            }
        }
    }

    fun start() {
        if (_state.value == StopwatchState.RUNNING) return

        val now = SystemClock.elapsedRealtime()
        if (_state.value == StopwatchState.IDLE) {
            sessionStartTimeEpoch = System.currentTimeMillis()
            accumulatedElapsed = 0L
            lapAccumulated = 0L
            _laps.value = emptyList()
            _bestLap.value = null
            _slowestLap.value = null
            _avgLapMillis.value = 0L
        }

        startTimestamp = now
        lapStartTimestamp = now
        _state.value = StopwatchState.RUNNING

        val settings = settingsRepo.settings.value
        HapticHelper.trigger(appContext, settings.hapticsEnabled, HapticHelper.HapticType.MEDIUM)
        SoundHelper.playStartCue(settings.soundCuesEnabled)

        persistCurrentState()
        startTicker()
        StopwatchService.startOrUpdateService(appContext)
    }

    fun pause() {
        if (_state.value != StopwatchState.RUNNING) return

        val now = SystemClock.elapsedRealtime()
        accumulatedElapsed += (now - startTimestamp)
        lapAccumulated += (now - lapStartTimestamp)

        _elapsedMillis.value = accumulatedElapsed
        _currentLapMillis.value = lapAccumulated
        _state.value = StopwatchState.PAUSED

        stopTicker()

        val settings = settingsRepo.settings.value
        HapticHelper.trigger(appContext, settings.hapticsEnabled, HapticHelper.HapticType.LIGHT)
        SoundHelper.playStopCue(settings.soundCuesEnabled)

        persistCurrentState()
        StopwatchService.startOrUpdateService(appContext)
    }

    fun resume() {
        if (_state.value != StopwatchState.PAUSED) return

        val now = SystemClock.elapsedRealtime()
        startTimestamp = now
        lapStartTimestamp = now
        _state.value = StopwatchState.RUNNING

        val settings = settingsRepo.settings.value
        HapticHelper.trigger(appContext, settings.hapticsEnabled, HapticHelper.HapticType.MEDIUM)
        SoundHelper.playStartCue(settings.soundCuesEnabled)

        persistCurrentState()
        startTicker()
        StopwatchService.startOrUpdateService(appContext)
    }

    fun toggleStartPause() {
        when (_state.value) {
            StopwatchState.IDLE -> start()
            StopwatchState.RUNNING -> pause()
            StopwatchState.PAUSED -> resume()
        }
    }

    fun recordLap(): Lap? {
        if (_state.value == StopwatchState.IDLE) return null

        val currentTotal = if (_state.value == StopwatchState.RUNNING) {
            val now = SystemClock.elapsedRealtime()
            accumulatedElapsed + (now - startTimestamp)
        } else {
            accumulatedElapsed
        }

        val currentLapTime = if (_state.value == StopwatchState.RUNNING) {
            val now = SystemClock.elapsedRealtime()
            lapAccumulated + (now - lapStartTimestamp)
        } else {
            lapAccumulated
        }

        val currentLapsList = _laps.value
        val lapNumber = currentLapsList.size + 1
        val previousLapTime = currentLapsList.firstOrNull()?.lapTimeMillis ?: currentLapTime
        val diff = if (currentLapsList.isEmpty()) 0L else currentLapTime - previousLapTime

        val newLap = Lap(
            id = System.currentTimeMillis() + lapNumber,
            lapNumber = lapNumber,
            lapTimeMillis = currentLapTime,
            totalTimeMillis = currentTotal,
            diffFromPreviousMillis = diff
        )

        // Reset lap timer
        val now = SystemClock.elapsedRealtime()
        lapAccumulated = 0L
        lapStartTimestamp = now
        _currentLapMillis.value = 0L

        val updatedList = listOf(newLap) + currentLapsList
        _laps.value = updatedList
        recalculateLapStats(updatedList)

        val settings = settingsRepo.settings.value
        HapticHelper.trigger(appContext, settings.hapticsEnabled, HapticHelper.HapticType.DOUBLE)
        SoundHelper.playLapCue(settings.soundCuesEnabled)

        persistCurrentState()
        StopwatchService.startOrUpdateService(appContext)
        return newLap
    }

    fun stopAndSave(sessionTitle: String? = null) {
        if (_state.value == StopwatchState.IDLE) return

        if (_state.value == StopwatchState.RUNNING) {
            pause()
        }

        val totalTime = _elapsedMillis.value
        val allLaps = _laps.value
        val lapCount = allLaps.size
        val best = _bestLap.value?.lapTimeMillis ?: 0L
        val worst = _slowestLap.value?.lapTimeMillis ?: 0L
        val avg = _avgLapMillis.value

        val title = sessionTitle ?: "Run Session • ${TimeFormatter.formatDate(sessionStartTimeEpoch)}"

        if (totalTime > 1000) {
            scope.launch(Dispatchers.IO) {
                val session = WorkoutSession(
                    title = title,
                    timestamp = sessionStartTimeEpoch,
                    durationMillis = totalTime,
                    lapCount = lapCount,
                    bestLapMillis = best,
                    avgLapMillis = avg,
                    slowestLapMillis = worst,
                    lapsData = formatLapsSummary(allLaps)
                )
                database.workoutSessionDao().insertSession(session)
            }
        }

        reset()
    }

    fun reset() {
        stopTicker()
        _state.value = StopwatchState.IDLE
        accumulatedElapsed = 0L
        startTimestamp = 0L
        lapAccumulated = 0L
        lapStartTimestamp = 0L
        _elapsedMillis.value = 0L
        _currentLapMillis.value = 0L
        _laps.value = emptyList()
        _bestLap.value = null
        _slowestLap.value = null
        _avgLapMillis.value = 0L

        settingsRepo.clearActiveSession()

        val settings = settingsRepo.settings.value
        HapticHelper.trigger(appContext, settings.hapticsEnabled, HapticHelper.HapticType.LIGHT)

        StopwatchService.stopService(appContext)
    }

    fun clearLaps() {
        _laps.value = emptyList()
        _bestLap.value = null
        _slowestLap.value = null
        _avgLapMillis.value = 0L
        lapAccumulated = _elapsedMillis.value
        lapStartTimestamp = SystemClock.elapsedRealtime()
        _currentLapMillis.value = lapAccumulated
        persistCurrentState()
    }

    fun deleteLap(lapNumber: Int) {
        val updated = _laps.value.filterNot { it.lapNumber == lapNumber }
        _laps.value = updated
        recalculateLapStats(updated)
        persistCurrentState()
    }

    private fun persistCurrentState() {
        settingsRepo.saveActiveSession(
            state = _state.value,
            accumulatedElapsed = accumulatedElapsed,
            startTimestamp = startTimestamp,
            lapAccumulated = lapAccumulated,
            lapStartTimestamp = lapStartTimestamp,
            sessionStartTimeEpoch = sessionStartTimeEpoch,
            laps = _laps.value
        )
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = scope.launch(Dispatchers.Default) {
            var loopCount = 0
            while (isActive && _state.value == StopwatchState.RUNNING) {
                val now = SystemClock.elapsedRealtime()
                val total = accumulatedElapsed + (now - startTimestamp)
                val lap = lapAccumulated + (now - lapStartTimestamp)
                _elapsedMillis.value = total
                _currentLapMillis.value = lap

                // Periodically save state in background every ~5 seconds
                loopCount++
                if (loopCount >= 200) {
                    loopCount = 0
                    persistCurrentState()
                }

                delay(16) // Smooth 60 FPS update rate
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun recalculateLapStats(lapsList: List<Lap>) {
        if (lapsList.isEmpty()) {
            _bestLap.value = null
            _slowestLap.value = null
            _avgLapMillis.value = 0L
            return
        }

        var minLap = lapsList.first()
        var maxLap = lapsList.first()
        var sum = 0L

        for (lap in lapsList) {
            sum += lap.lapTimeMillis
            if (lap.lapTimeMillis < minLap.lapTimeMillis) {
                minLap = lap
            }
            if (lap.lapTimeMillis > maxLap.lapTimeMillis) {
                maxLap = lap
            }
        }

        _bestLap.value = minLap
        _slowestLap.value = if (lapsList.size > 1) maxLap else null
        _avgLapMillis.value = sum / lapsList.size
    }

    private fun formatLapsSummary(laps: List<Lap>): String {
        val sb = StringBuilder()
        for (lap in laps.reversed()) {
            sb.append("Lap ${lap.lapNumber}: ${TimeFormatter.format(lap.lapTimeMillis)} (Total: ${TimeFormatter.format(lap.totalTimeMillis)})\n")
        }
        return sb.toString()
    }

    companion object {
        @Volatile
        private var INSTANCE: StopwatchManager? = null

        fun getInstance(context: Context): StopwatchManager {
            return INSTANCE ?: synchronized(this) {
                val instance = StopwatchManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
