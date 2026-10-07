package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PrecisionMode
import com.example.util.TimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RunStop", appName)
    }

    @Test
    fun `test time formatter centiseconds`() {
        // 1 minute, 23 seconds, 450 ms = 83450 ms
        val formatted = TimeFormatter.format(83450L, PrecisionMode.CENTISECONDS)
        assertEquals("01:23.45", formatted)
    }

    @Test
    fun `test time formatter lap diff`() {
        val fasterDiff = TimeFormatter.formatLapDifference(-2150L)
        assertTrue(fasterDiff.startsWith("-"))

        val slowerDiff = TimeFormatter.formatLapDifference(1500L)
        assertTrue(slowerDiff.startsWith("+"))
    }
}
