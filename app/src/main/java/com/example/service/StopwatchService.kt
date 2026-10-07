package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.PrecisionMode
import com.example.model.StopwatchState
import com.example.util.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopwatchService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var notifUpdateJob: Job? = null
    private lateinit var stopwatchManager: StopwatchManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        stopwatchManager = StopwatchManager.getInstance(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> stopwatchManager.start()
            ACTION_PAUSE -> stopwatchManager.pause()
            ACTION_RESUME -> stopwatchManager.resume()
            ACTION_LAP -> stopwatchManager.recordLap()
            ACTION_STOP -> {
                stopwatchManager.stopAndSave()
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_RESET -> {
                stopwatchManager.reset()
                stopForegroundService()
                return START_NOT_STICKY
            }
        }

        if (stopwatchManager.state.value == StopwatchState.IDLE) {
            stopForegroundService()
            return START_NOT_STICKY
        }

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startNotificationTicker()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        notifUpdateJob?.cancel()
    }

    private fun startNotificationTicker() {
        notifUpdateJob?.cancel()
        notifUpdateJob = serviceScope.launch(Dispatchers.Default) {
            while (isActive) {
                if (stopwatchManager.state.value == StopwatchState.RUNNING) {
                    val notif = buildNotification()
                    notificationManager.notify(NOTIFICATION_ID, notif)
                }
                delay(1000) // Update notification once per second
            }
        }
    }

    private fun stopForegroundService() {
        notifUpdateJob?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val state = stopwatchManager.state.value
        val totalMillis = stopwatchManager.elapsedMillis.value
        val lapMillis = stopwatchManager.currentLapMillis.value
        val lapCount = stopwatchManager.laps.value.size + 1

        val timeStr = TimeFormatter.format(totalMillis, PrecisionMode.CENTISECONDS)
        val lapStr = TimeFormatter.format(lapMillis, PrecisionMode.CENTISECONDS)

        val title = when (state) {
            StopwatchState.RUNNING -> "ARVEXA • Running"
            StopwatchState.PAUSED -> "ARVEXA • Paused"
            StopwatchState.IDLE -> "ARVEXA"
        }

        val contentText = if (state == StopwatchState.RUNNING || state == StopwatchState.PAUSED) {
            "Time: $timeStr  |  Lap $lapCount: $lapStr"
        } else {
            "Ready to run"
        }

        // Open MainActivity when tapping notification
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(contentPendingIntent)
            .setOngoing(state == StopwatchState.RUNNING)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)

        // Action Buttons
        if (state == StopwatchState.RUNNING) {
            // Pause action
            val pauseIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_PAUSE }
            val pausePI = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePI)

            // Lap action
            val lapIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_LAP }
            val lapPI = PendingIntent.getService(this, 2, lapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_menu_agenda, "Lap", lapPI)

            // Stop action
            val stopIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_STOP }
            val stopPI = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPI)
        } else if (state == StopwatchState.PAUSED) {
            // Resume action
            val resumeIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_RESUME }
            val resumePI = PendingIntent.getService(this, 4, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "Resume", resumePI)

            // Reset action
            val resetIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_RESET }
            val resetPI = PendingIntent.getService(this, 6, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_menu_revert, "Reset", resetPI)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RunStop Active Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing stopwatch notification with playback controls"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "runstop_stopwatch_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.action.START"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_RESUME = "com.example.action.RESUME"
        const val ACTION_LAP = "com.example.action.LAP"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_RESET = "com.example.action.RESET"

        fun startOrUpdateService(context: Context) {
            val intent = Intent(context, StopwatchService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, StopwatchService::class.java)
            context.stopService(intent)
        }
    }
}
