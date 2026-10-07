package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticHelper {
    fun trigger(context: Context, enabled: Boolean, type: HapticType = HapticType.MEDIUM) {
        if (!enabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = when (type) {
                        HapticType.LIGHT -> VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
                        HapticType.MEDIUM -> VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                        HapticType.HEAVY -> VibrationEffect.createOneShot(55, VibrationEffect.DEFAULT_AMPLITUDE)
                        HapticType.DOUBLE -> VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 30), -1)
                        HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 40, 70, 50), -1)
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(type.fallbackDuration)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors gracefully
        }
    }

    fun performViewHaptic(view: View?, enabled: Boolean) {
        if (!enabled || view == null) return
        try {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (e: Exception) {
            // Ignore
        }
    }

    enum class HapticType(val fallbackDuration: Long) {
        LIGHT(15L),
        MEDIUM(35L),
        HEAVY(60L),
        DOUBLE(100L),
        SUCCESS(120L)
    }
}
