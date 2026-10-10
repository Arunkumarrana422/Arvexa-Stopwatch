package com.example.util

import com.example.model.PrecisionMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object TimeFormatter {

    data class FormattedTimeParts(
        val hours: String,
        val minutes: String,
        val seconds: String,
        val fraction: String,
        val mainDisplay: String,
        val fractionDisplay: String
    )

    fun getTimeParts(millis: Long, precision: PrecisionMode = PrecisionMode.CENTISECONDS): FormattedTimeParts {
        val safeMillis = if (millis < 0) 0L else millis
        val hours = safeMillis / 3600000
        val remainingAfterHours = safeMillis % 3600000
        val minutes = remainingAfterHours / 60000
        val remainingAfterMinutes = remainingAfterHours % 60000
        val seconds = remainingAfterMinutes / 1000
        val ms = remainingAfterMinutes % 1000

        val hStr = String.format(Locale.US, "%02d", hours)
        val mStr = String.format(Locale.US, "%02d", minutes)
        val sStr = String.format(Locale.US, "%02d", seconds)

        val fracStr = when (precision) {
            PrecisionMode.CENTISECONDS -> String.format(Locale.US, "%02d", ms / 10)
            PrecisionMode.MILLISECONDS -> String.format(Locale.US, "%03d", ms)
            PrecisionMode.SECONDS -> ""
        }

        val (main, frac) = if (hours > 0) {
            Pair("$hStr:$mStr", ":$sStr")
        } else {
            val f = if (fracStr.isNotEmpty()) ".$fracStr" else ""
            Pair("$mStr:$sStr", f)
        }

        return FormattedTimeParts(
            hours = hStr,
            minutes = mStr,
            seconds = sStr,
            fraction = fracStr,
            mainDisplay = main,
            fractionDisplay = frac
        )
    }

    fun format(millis: Long, precision: PrecisionMode = PrecisionMode.CENTISECONDS, showHoursAlways: Boolean = false): String {
        val safeMillis = if (millis < 0) 0L else millis
        val hours = safeMillis / 3600000
        val remainingAfterHours = safeMillis % 3600000
        val minutes = remainingAfterHours / 60000
        val remainingAfterMinutes = remainingAfterHours % 60000
        val seconds = remainingAfterMinutes / 1000
        val ms = remainingAfterMinutes % 1000

        val timeWithoutMs = if (showHoursAlways || hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }

        return when (precision) {
            PrecisionMode.CENTISECONDS -> "$timeWithoutMs.${String.format(Locale.US, "%02d", ms / 10)}"
            PrecisionMode.MILLISECONDS -> "$timeWithoutMs.${String.format(Locale.US, "%03d", ms)}"
            PrecisionMode.SECONDS -> timeWithoutMs
        }
    }

    fun formatLapDifference(diffMillis: Long): String {
        val sign = if (diffMillis > 0) "+" else if (diffMillis < 0) "-" else "±"
        val absMillis = abs(diffMillis)
        val minutes = (absMillis % 3600000) / 60000
        val seconds = (absMillis % 60000) / 1000
        val cs = (absMillis % 1000) / 10

        return "$sign${String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, cs)}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDurationShort(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        return if (hours > 0) {
            "${hours}h ${minutes}m ${seconds}s"
        } else if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }
}
