package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.data.SettingsRepository
import com.example.model.StopwatchState
import com.example.model.VolumeAction
import com.example.service.StopwatchManager
import com.example.ui.RunStopApp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var stopwatchManager: StopwatchManager
    private lateinit var settingsRepository: SettingsRepository

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Notification permission result handled
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dismiss Android 12+ system splash screen immediately so our custom splash screen takes over
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }

        enableEdgeToEdge()

        stopwatchManager = StopwatchManager.getInstance(applicationContext)
        settingsRepository = SettingsRepository.getInstance(applicationContext)

        // Request notification permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Manage keep screen awake dynamically
        lifecycleScope.launch {
            stopwatchManager.state.collectLatest { state ->
                val keepAwake = settingsRepository.settings.value.keepScreenAwake
                if (keepAwake && state == StopwatchState.RUNNING) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        // Auto start if configured
        if (settingsRepository.settings.value.autoStartOnLaunch && stopwatchManager.state.value == StopwatchState.IDLE) {
            stopwatchManager.start()
        }

        setContent {
            RunStopApp(
                stopwatchManager = stopwatchManager,
                settingsRepository = settingsRepository
            )
        }
    }

    private var isVolumeUpPressed = false
    private var isVolumeDownPressed = false

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val settings = settingsRepository.settings.value
        if (settings.volumeControlEnabled) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    // Trigger only on the first press; ignore repeated key events while held down
                    if (event?.repeatCount == 0 && !isVolumeUpPressed) {
                        isVolumeUpPressed = true
                        executeVolumeAction(settings.volumeUpAction)
                    }
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    // Trigger only on the first press; ignore repeated key events while held down
                    if (event?.repeatCount == 0 && !isVolumeDownPressed) {
                        isVolumeDownPressed = true
                        executeVolumeAction(settings.volumeDownAction)
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val settings = settingsRepository.settings.value
        if (settings.volumeControlEnabled) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    isVolumeUpPressed = false
                    return true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    isVolumeDownPressed = false
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun executeVolumeAction(action: VolumeAction) {
        when (action) {
            VolumeAction.START_PAUSE -> stopwatchManager.toggleStartPause()
            VolumeAction.LAP -> {
                if (stopwatchManager.state.value == com.example.model.StopwatchState.RUNNING) {
                    stopwatchManager.recordLap()
                }
            }
            VolumeAction.STOP -> stopwatchManager.stopAndSave()
            VolumeAction.RESET -> stopwatchManager.reset()
            VolumeAction.NONE -> { /* No-op */ }
        }
    }
}
