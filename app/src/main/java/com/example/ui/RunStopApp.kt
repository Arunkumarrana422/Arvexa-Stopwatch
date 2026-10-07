package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.SettingsRepository
import com.example.model.AppScreen
import com.example.service.StopwatchManager
import com.example.ui.components.CustomBottomNav
import com.example.ui.components.LockScreenOverlay
import com.example.ui.screens.LapsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.RunStopTheme

@Composable
fun RunStopApp(
    stopwatchManager: StopwatchManager,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val settings by settingsRepository.settings.collectAsState()
    val laps by stopwatchManager.laps.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.STOPWATCH) }
    var showSplash by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }

    RunStopTheme(themeMode = settings.themeMode) {
        if (showSplash) {
            SplashScreen(onSplashFinished = { showSplash = false })
        } else {
            // Handle Android back button
            BackHandler(enabled = !isScreenLocked && currentScreen != AppScreen.STOPWATCH) {
                currentScreen = AppScreen.STOPWATCH
            }

            Box(modifier = modifier.fillMaxSize()) {
                Scaffold(
                    bottomBar = {
                        CustomBottomNav(
                            currentScreen = currentScreen,
                            onScreenSelected = { currentScreen = it },
                            lapCount = laps.size,
                            hapticsEnabled = settings.hapticsEnabled
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = AppTheme.backgroundGradient)
                            .padding(innerPadding)
                    ) {
                        if (settings.animationsEnabled) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    val targetIndex = targetState.ordinal
                                    val initialIndex = initialState.ordinal
                                    val duration = 280
                                    if (targetIndex > initialIndex) {
                                        (slideInHorizontally(
                                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                            initialOffsetX = { it / 3 }
                                        ) + fadeIn(animationSpec = tween(duration))) togetherWith
                                                (slideOutHorizontally(
                                                    animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                                    targetOffsetX = { -it / 3 }
                                                ) + fadeOut(animationSpec = tween(duration / 2)))
                                    } else {
                                        (slideInHorizontally(
                                            animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                            initialOffsetX = { -it / 3 }
                                        ) + fadeIn(animationSpec = tween(duration))) togetherWith
                                                (slideOutHorizontally(
                                                    animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                                    targetOffsetX = { it / 3 }
                                                ) + fadeOut(animationSpec = tween(duration / 2)))
                                    }
                                },
                                label = "screen_smooth_transition"
                            ) { screen ->
                                when (screen) {
                                    AppScreen.STOPWATCH -> StopwatchScreen(
                                        stopwatchManager = stopwatchManager,
                                        precisionMode = settings.precision,
                                        themeMode = settings.themeMode,
                                        onThemeChanged = { settingsRepository.updateSettings(settings.copy(themeMode = it)) },
                                        onNavigateTo = { currentScreen = it },
                                        onLockScreen = { isScreenLocked = true }
                                    )

                                    AppScreen.LAPS -> LapsScreen(
                                        stopwatchManager = stopwatchManager,
                                        precisionMode = settings.precision
                                    )

                                    AppScreen.STATS -> StatsScreen(
                                        stopwatchManager = stopwatchManager,
                                        precisionMode = settings.precision
                                    )

                                    AppScreen.SETTINGS -> SettingsScreen(
                                        settingsRepository = settingsRepository
                                    )
                                }
                            }
                        } else {
                            when (currentScreen) {
                                AppScreen.STOPWATCH -> StopwatchScreen(
                                    stopwatchManager = stopwatchManager,
                                    precisionMode = settings.precision,
                                    themeMode = settings.themeMode,
                                    onThemeChanged = { settingsRepository.updateSettings(settings.copy(themeMode = it)) },
                                    onNavigateTo = { currentScreen = it },
                                    onLockScreen = { isScreenLocked = true }
                                )

                                AppScreen.LAPS -> LapsScreen(
                                    stopwatchManager = stopwatchManager,
                                    precisionMode = settings.precision
                                )

                                AppScreen.STATS -> StatsScreen(
                                    stopwatchManager = stopwatchManager,
                                    precisionMode = settings.precision
                                )

                                AppScreen.SETTINGS -> SettingsScreen(
                                    settingsRepository = settingsRepository
                                )
                            }
                        }
                    }
                }

                // Full-screen Lock Overlay protecting entire screen from accidental touch
                if (isScreenLocked) {
                    LockScreenOverlay(
                        onUnlock = { isScreenLocked = false },
                        stopwatchManager = stopwatchManager,
                        precisionMode = settings.precision
                    )
                }
            }
        }
    }
}
