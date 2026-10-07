package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Lap
import com.example.model.PrecisionMode
import com.example.model.StopwatchSettings
import com.example.model.StopwatchState
import com.example.model.ThemeMode
import com.example.model.VolumeAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class PersistedStopwatchState(
    val state: StopwatchState = StopwatchState.IDLE,
    val accumulatedElapsed: Long = 0L,
    val startTimestamp: Long = 0L,
    val lapAccumulated: Long = 0L,
    val lapStartTimestamp: Long = 0L,
    val sessionStartTimeEpoch: Long = 0L,
    val laps: List<Lap> = emptyList()
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("runstop_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<StopwatchSettings> = _settings.asStateFlow()

    fun updateSettings(newSettings: StopwatchSettings) {
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    private fun loadSettings(): StopwatchSettings {
        return StopwatchSettings(
            autoStartOnLaunch = prefs.getBoolean(KEY_AUTO_START, false),
            keepScreenAwake = prefs.getBoolean(KEY_KEEP_AWAKE, true),
            precision = try {
                PrecisionMode.valueOf(prefs.getString(KEY_PRECISION, PrecisionMode.CENTISECONDS.name) ?: PrecisionMode.CENTISECONDS.name)
            } catch (e: Exception) {
                PrecisionMode.CENTISECONDS
            },
            volumeControlEnabled = prefs.getBoolean(KEY_VOLUME_CONTROL, true),
            volumeUpAction = try {
                VolumeAction.valueOf(prefs.getString(KEY_VOL_UP_ACTION, VolumeAction.START_PAUSE.name) ?: VolumeAction.START_PAUSE.name)
            } catch (e: Exception) {
                VolumeAction.START_PAUSE
            },
            volumeDownAction = try {
                VolumeAction.valueOf(prefs.getString(KEY_VOL_DOWN_ACTION, VolumeAction.LAP.name) ?: VolumeAction.LAP.name)
            } catch (e: Exception) {
                VolumeAction.LAP
            },
            showOngoingNotification = prefs.getBoolean(KEY_SHOW_NOTIFICATION, true),
            showLapInNotification = prefs.getBoolean(KEY_SHOW_LAP_NOTIF, true),
            soundCuesEnabled = prefs.getBoolean(KEY_SOUND_CUES, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            themeMode = try {
                ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
            } catch (e: Exception) {
                ThemeMode.DARK
            },
            animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS, true)
        )
    }

    private fun saveSettings(s: StopwatchSettings) {
        prefs.edit().apply {
            putBoolean(KEY_AUTO_START, s.autoStartOnLaunch)
            putBoolean(KEY_KEEP_AWAKE, s.keepScreenAwake)
            putString(KEY_PRECISION, s.precision.name)
            putBoolean(KEY_VOLUME_CONTROL, s.volumeControlEnabled)
            putString(KEY_VOL_UP_ACTION, s.volumeUpAction.name)
            putString(KEY_VOL_DOWN_ACTION, s.volumeDownAction.name)
            putBoolean(KEY_SHOW_NOTIFICATION, s.showOngoingNotification)
            putBoolean(KEY_SHOW_LAP_NOTIF, s.showLapInNotification)
            putBoolean(KEY_SOUND_CUES, s.soundCuesEnabled)
            putBoolean(KEY_HAPTICS, s.hapticsEnabled)
            putString(KEY_THEME_MODE, s.themeMode.name)
            putBoolean(KEY_ANIMATIONS, s.animationsEnabled)
            apply()
        }
    }

    fun saveActiveSession(
        state: StopwatchState,
        accumulatedElapsed: Long,
        startTimestamp: Long,
        lapAccumulated: Long,
        lapStartTimestamp: Long,
        sessionStartTimeEpoch: Long,
        laps: List<Lap>
    ) {
        prefs.edit().apply {
            putString(KEY_ACTIVE_STATE, state.name)
            putLong(KEY_ACTIVE_ACC_ELAPSED, accumulatedElapsed)
            putLong(KEY_ACTIVE_START_TIME, startTimestamp)
            putLong(KEY_ACTIVE_LAP_ACC, lapAccumulated)
            putLong(KEY_ACTIVE_LAP_START, lapStartTimestamp)
            putLong(KEY_ACTIVE_SESSION_EPOCH, sessionStartTimeEpoch)
            putString(KEY_ACTIVE_LAPS_JSON, serializeLaps(laps))
            apply()
        }
    }

    fun loadActiveSession(): PersistedStopwatchState {
        val stateName = prefs.getString(KEY_ACTIVE_STATE, StopwatchState.IDLE.name) ?: StopwatchState.IDLE.name
        val state = try {
            StopwatchState.valueOf(stateName)
        } catch (e: Exception) {
            StopwatchState.IDLE
        }

        if (state == StopwatchState.IDLE) {
            return PersistedStopwatchState()
        }

        val lapsJson = prefs.getString(KEY_ACTIVE_LAPS_JSON, "") ?: ""
        val laps = deserializeLaps(lapsJson)

        return PersistedStopwatchState(
            state = state,
            accumulatedElapsed = prefs.getLong(KEY_ACTIVE_ACC_ELAPSED, 0L),
            startTimestamp = prefs.getLong(KEY_ACTIVE_START_TIME, 0L),
            lapAccumulated = prefs.getLong(KEY_ACTIVE_LAP_ACC, 0L),
            lapStartTimestamp = prefs.getLong(KEY_ACTIVE_LAP_START, 0L),
            sessionStartTimeEpoch = prefs.getLong(KEY_ACTIVE_SESSION_EPOCH, System.currentTimeMillis()),
            laps = laps
        )
    }

    fun clearActiveSession() {
        prefs.edit().apply {
            remove(KEY_ACTIVE_STATE)
            remove(KEY_ACTIVE_ACC_ELAPSED)
            remove(KEY_ACTIVE_START_TIME)
            remove(KEY_ACTIVE_LAP_ACC)
            remove(KEY_ACTIVE_LAP_START)
            remove(KEY_ACTIVE_SESSION_EPOCH)
            remove(KEY_ACTIVE_LAPS_JSON)
            apply()
        }
    }

    private fun serializeLaps(laps: List<Lap>): String {
        val jsonArray = JSONArray()
        for (lap in laps) {
            val obj = JSONObject().apply {
                put("id", lap.id)
                put("lapNumber", lap.lapNumber)
                put("lapTimeMillis", lap.lapTimeMillis)
                put("totalTimeMillis", lap.totalTimeMillis)
                put("diffFromPreviousMillis", lap.diffFromPreviousMillis)
                put("timestamp", lap.timestamp)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun deserializeLaps(jsonString: String): List<Lap> {
        if (jsonString.isEmpty()) return emptyList()
        val result = mutableListOf<Lap>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    Lap(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        lapNumber = obj.optInt("lapNumber", i + 1),
                        lapTimeMillis = obj.optLong("lapTimeMillis", 0L),
                        totalTimeMillis = obj.optLong("totalTimeMillis", 0L),
                        diffFromPreviousMillis = obj.optLong("diffFromPreviousMillis", 0L),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return result
    }

    companion object {
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_KEEP_AWAKE = "keep_awake"
        private const val KEY_PRECISION = "precision"
        private const val KEY_VOLUME_CONTROL = "volume_control"
        private const val KEY_VOL_UP_ACTION = "vol_up_action"
        private const val KEY_VOL_DOWN_ACTION = "vol_down_action"
        private const val KEY_SHOW_NOTIFICATION = "show_notification"
        private const val KEY_SHOW_LAP_NOTIF = "show_lap_notif"
        private const val KEY_SOUND_CUES = "sound_cues"
        private const val KEY_HAPTICS = "haptics"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ANIMATIONS = "animations"

        // Active stopwatch session persistence keys
        private const val KEY_ACTIVE_STATE = "active_stopwatch_state"
        private const val KEY_ACTIVE_ACC_ELAPSED = "active_acc_elapsed"
        private const val KEY_ACTIVE_START_TIME = "active_start_time"
        private const val KEY_ACTIVE_LAP_ACC = "active_lap_acc"
        private const val KEY_ACTIVE_LAP_START = "active_lap_start"
        private const val KEY_ACTIVE_SESSION_EPOCH = "active_session_epoch"
        private const val KEY_ACTIVE_LAPS_JSON = "active_laps_json"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
