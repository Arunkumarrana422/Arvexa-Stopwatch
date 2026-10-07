package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.Lap
import com.example.model.PrecisionMode
import com.example.model.WorkoutSession
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object LapPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_X = 28f

    // ─────────────────────────────────────────────────────────────
    // 1. LIVE LAPS SCREEN PDF EXPORT
    // ─────────────────────────────────────────────────────────────
    fun generateAndShareLapReport(
        context: Context,
        laps: List<Lap>,
        totalElapsedMillis: Long,
        bestLap: Lap?,
        slowestLap: Lap?,
        avgLapMillis: Long,
        precisionMode: PrecisionMode
    ) {
        val pdfFile = generateLapPdfFile(
            context = context,
            laps = laps,
            totalElapsedMillis = totalElapsedMillis,
            bestLap = bestLap,
            slowestLap = slowestLap,
            avgLapMillis = avgLapMillis,
            precisionMode = precisionMode
        ) ?: return

        sharePdfFile(
            context = context,
            pdfFile = pdfFile,
            subject = "Arvexa Stopwatch • Lap Timing Report",
            message = "Here is the official Arvexa Stopwatch Lap Timing Report attached as a PDF."
        )
    }

    // ─────────────────────────────────────────────────────────────
    // 2. WORKOUT HISTORY SCREEN PDF EXPORT (ALL SESSIONS)
    // ─────────────────────────────────────────────────────────────
    fun generateAndShareWorkoutHistoryReport(
        context: Context,
        sessions: List<WorkoutSession>,
        precisionMode: PrecisionMode
    ) {
        if (sessions.isEmpty()) return

        val pdfFile = generateWorkoutHistoryPdfFile(
            context = context,
            sessions = sessions,
            precisionMode = precisionMode
        ) ?: return

        sharePdfFile(
            context = context,
            pdfFile = pdfFile,
            subject = "Arvexa Stopwatch • Workout History Log",
            message = "Here is the official Arvexa Stopwatch Workout History and Training Log attached as a PDF."
        )
    }

    // ─────────────────────────────────────────────────────────────
    // 3. SINGLE WORKOUT SESSION PDF EXPORT
    // ─────────────────────────────────────────────────────────────
    fun generateAndShareSingleWorkoutSessionReport(
        context: Context,
        session: WorkoutSession,
        precisionMode: PrecisionMode
    ) {
        val pdfFile = generateSingleWorkoutSessionPdfFile(
            context = context,
            session = session,
            precisionMode = precisionMode
        ) ?: return

        sharePdfFile(
            context = context,
            pdfFile = pdfFile,
            subject = "Arvexa Stopwatch • ${session.title}",
            message = "Here is the official Arvexa Stopwatch report for \"${session.title}\" attached as a PDF."
        )
    }

    // ─────────────────────────────────────────────────────────────
    // PDF GENERATION: LAP TIMING REPORT
    // ─────────────────────────────────────────────────────────────
    private fun generateLapPdfFile(
        context: Context,
        laps: List<Lap>,
        totalElapsedMillis: Long,
        bestLap: Lap?,
        slowestLap: Lap?,
        avgLapMillis: Long,
        precisionMode: PrecisionMode
    ): File? {
        val pdfDocument = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        val lapsOnFirstPage = 22
        val lapsOnSubsequentPages = 30
        val remainingLaps = (laps.size - lapsOnFirstPage).coerceAtLeast(0)
        val totalPages = if (laps.size <= lapsOnFirstPage) 1 else 1 + ((remainingLaps + lapsOnSubsequentPages - 1) / lapsOnSubsequentPages)

        var lapIndex = 0
        val currentDateStr = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.US).format(Date())

        for (pageNum in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            fillPaint.color = Color.WHITE
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), fillPaint)

            var currentY: Float

            if (pageNum == 1) {
                drawFullHeader(
                    context = context,
                    canvas = canvas,
                    fillPaint = fillPaint,
                    textPaint = textPaint,
                    dateStr = currentDateStr,
                    subtitleText = "PRO LAP TIMING & PERFORMANCE REPORT"
                )

                drawKpiSummaryCards(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                    textPaint = textPaint,
                    startY = 106f,
                    totalElapsedMillis = totalElapsedMillis,
                    lapCount = laps.size,
                    bestLap = bestLap,
                    avgLapMillis = avgLapMillis,
                    precisionMode = precisionMode
                )

                textPaint.apply {
                    textSize = 10.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    color = 0xFF0F172A.toInt()
                }
                canvas.drawText("INDIVIDUAL LAP SPLITS", MARGIN_X, 202f, textPaint)
                currentY = 210f
            } else {
                drawCompactHeader(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    textPaint = textPaint,
                    pageNum = pageNum,
                    totalPages = totalPages,
                    dateStr = currentDateStr,
                    titleText = "ARVEXA STOPWATCH • LAP REPORT"
                )
                currentY = 56f
            }

            drawTableHeader(canvas, fillPaint, textPaint, currentY)
            currentY += 24f

            val lapsToDraw = if (pageNum == 1) lapsOnFirstPage else lapsOnSubsequentPages
            val endIndex = (lapIndex + lapsToDraw).coerceAtMost(laps.size)
            val rowHeight = 22f

            for (i in lapIndex until endIndex) {
                val lap = laps[i]
                val isAlternate = (i % 2 == 1)
                val isBest = bestLap != null && lap.lapNumber == bestLap.lapNumber
                val isSlowest = slowestLap != null && lap.lapNumber == slowestLap.lapNumber && laps.size > 1

                drawTableRow(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                    textPaint = textPaint,
                    y = currentY,
                    rowHeight = rowHeight,
                    lap = lap,
                    isAlternate = isAlternate,
                    isBest = isBest,
                    isSlowest = isSlowest,
                    precisionMode = precisionMode
                )
                currentY += rowHeight
            }
            lapIndex = endIndex

            drawPageFooter(canvas, strokePaint, textPaint, pageNum, totalPages)
            pdfDocument.finishPage(page)
        }

        return savePdfToCache(context, pdfDocument, "Arvexa_Lap_Report_${System.currentTimeMillis()}.pdf")
    }

    // ─────────────────────────────────────────────────────────────
    // PDF GENERATION: WORKOUT HISTORY (ALL SESSIONS)
    // ─────────────────────────────────────────────────────────────
    private fun generateWorkoutHistoryPdfFile(
        context: Context,
        sessions: List<WorkoutSession>,
        precisionMode: PrecisionMode
    ): File? {
        val pdfDocument = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        val sessionsOnFirstPage = 18
        val sessionsOnSubsequentPages = 26
        val remaining = (sessions.size - sessionsOnFirstPage).coerceAtLeast(0)
        val totalPages = if (sessions.size <= sessionsOnFirstPage) 1 else 1 + ((remaining + sessionsOnSubsequentPages - 1) / sessionsOnSubsequentPages)

        var sessionIndex = 0
        val currentDateStr = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.US).format(Date())

        val totalTime = sessions.sumOf { it.durationMillis }
        val totalLaps = sessions.sumOf { it.lapCount }
        val bestEver = sessions.map { it.bestLapMillis }.filter { it > 0 }.minOrNull() ?: 0L

        for (pageNum in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            fillPaint.color = Color.WHITE
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), fillPaint)

            var currentY: Float

            if (pageNum == 1) {
                drawFullHeader(
                    context = context,
                    canvas = canvas,
                    fillPaint = fillPaint,
                    textPaint = textPaint,
                    dateStr = currentDateStr,
                    subtitleText = "OFFICIAL WORKOUT HISTORY & PERFORMANCE LOG"
                )

                drawWorkoutHistoryKpis(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                    textPaint = textPaint,
                    startY = 106f,
                    sessionCount = sessions.size,
                    totalDurationMillis = totalTime,
                    totalLaps = totalLaps,
                    bestLapMillis = bestEver,
                    precisionMode = precisionMode
                )

                textPaint.apply {
                    textSize = 10.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    color = 0xFF0F172A.toInt()
                }
                canvas.drawText("RECORDED SESSIONS LOG", MARGIN_X, 202f, textPaint)
                currentY = 210f
            } else {
                drawCompactHeader(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    textPaint = textPaint,
                    pageNum = pageNum,
                    totalPages = totalPages,
                    dateStr = currentDateStr,
                    titleText = "ARVEXA STOPWATCH • WORKOUT HISTORY"
                )
                currentY = 56f
            }

            drawWorkoutTableHeader(canvas, fillPaint, textPaint, currentY)
            currentY += 24f

            val countToDraw = if (pageNum == 1) sessionsOnFirstPage else sessionsOnSubsequentPages
            val endIndex = (sessionIndex + countToDraw).coerceAtMost(sessions.size)
            val rowHeight = 26f

            for (i in sessionIndex until endIndex) {
                val session = sessions[i]
                val isAlternate = (i % 2 == 1)
                drawWorkoutTableRow(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                    textPaint = textPaint,
                    y = currentY,
                    rowHeight = rowHeight,
                    index = i + 1,
                    session = session,
                    isAlternate = isAlternate,
                    precisionMode = precisionMode
                )
                currentY += rowHeight
            }
            sessionIndex = endIndex

            drawPageFooter(canvas, strokePaint, textPaint, pageNum, totalPages)
            pdfDocument.finishPage(page)
        }

        return savePdfToCache(context, pdfDocument, "Arvexa_Workout_History_${System.currentTimeMillis()}.pdf")
    }

    // ─────────────────────────────────────────────────────────────
    // PDF GENERATION: SINGLE WORKOUT SESSION
    // ─────────────────────────────────────────────────────────────
    private fun generateSingleWorkoutSessionPdfFile(
        context: Context,
        session: WorkoutSession,
        precisionMode: PrecisionMode
    ): File? {
        val pdfDocument = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        val lapLines = session.lapsData.lines().filter { it.isNotBlank() }
        val lapsOnFirstPage = 22
        val lapsOnSubsequentPages = 30
        val remaining = (lapLines.size - lapsOnFirstPage).coerceAtLeast(0)
        val totalPages = if (lapLines.size <= lapsOnFirstPage) 1 else 1 + ((remaining + lapsOnSubsequentPages - 1) / lapsOnSubsequentPages)

        var lapIndex = 0
        val sessionDateStr = TimeFormatter.formatDate(session.timestamp)

        for (pageNum in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            fillPaint.color = Color.WHITE
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), fillPaint)

            var currentY: Float

            if (pageNum == 1) {
                drawFullHeader(
                    context = context,
                    canvas = canvas,
                    fillPaint = fillPaint,
                    textPaint = textPaint,
                    dateStr = sessionDateStr,
                    subtitleText = "WORKOUT SESSION PERFORMANCE REPORT"
                )

                drawSingleSessionKpis(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    strokePaint = strokePaint,
                    textPaint = textPaint,
                    startY = 106f,
                    session = session,
                    precisionMode = precisionMode
                )

                textPaint.apply {
                    textSize = 10.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    color = 0xFF0F172A.toInt()
                }
                val titleDisplay = if (session.title.length > 50) session.title.take(47) + "..." else session.title
                canvas.drawText("SESSION LAPS: $titleDisplay", MARGIN_X, 202f, textPaint)
                currentY = 210f
            } else {
                drawCompactHeader(
                    canvas = canvas,
                    fillPaint = fillPaint,
                    textPaint = textPaint,
                    pageNum = pageNum,
                    totalPages = totalPages,
                    dateStr = sessionDateStr,
                    titleText = "ARVEXA STOPWATCH • ${session.title}"
                )
                currentY = 56f
            }

            if (lapLines.isNotEmpty()) {
                drawSingleSessionTableHeader(canvas, fillPaint, textPaint, currentY)
                currentY += 24f

                val countToDraw = if (pageNum == 1) lapsOnFirstPage else lapsOnSubsequentPages
                val endIndex = (lapIndex + countToDraw).coerceAtMost(lapLines.size)
                val rowHeight = 22f

                for (i in lapIndex until endIndex) {
                    val line = lapLines[i]
                    val isAlternate = (i % 2 == 1)
                    drawSingleSessionTableRow(
                        canvas = canvas,
                        fillPaint = fillPaint,
                        strokePaint = strokePaint,
                        textPaint = textPaint,
                        y = currentY,
                        rowHeight = rowHeight,
                        lapSummaryLine = line,
                        isAlternate = isAlternate
                    )
                    currentY += rowHeight
                }
                lapIndex = endIndex
            }

            drawPageFooter(canvas, strokePaint, textPaint, pageNum, totalPages)
            pdfDocument.finishPage(page)
        }

        return savePdfToCache(context, pdfDocument, "Arvexa_Workout_${session.id}_${session.timestamp}.pdf")
    }

    // ─────────────────────────────────────────────────────────────
    // HEADER DRAWING METHODS
    // ─────────────────────────────────────────────────────────────
    private fun drawFullHeader(
        context: Context,
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        textPaint: Paint,
        dateStr: String,
        subtitleText: String
    ) {
        fillPaint.shader = LinearGradient(
            0f, 0f, PAGE_WIDTH.toFloat(), 0f,
            intArrayOf(0xFF070B17.toInt(), 0xFF0F172A.toInt(), 0xFF182238.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 92f, fillPaint)
        fillPaint.shader = null

        fillPaint.shader = LinearGradient(
            0f, 92f, PAGE_WIDTH.toFloat(), 92f,
            intArrayOf(0xFF00C2FF.toInt(), 0xFF6C5CE7.toInt(), 0xFFFF2A6D.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 92f, PAGE_WIDTH.toFloat(), 95f, fillPaint)
        fillPaint.shader = null

        val logoDrawable = ContextCompat.getDrawable(context, R.drawable.ic_app_logo)
        if (logoDrawable != null) {
            val logoSize = 48
            val logoBitmap = Bitmap.createBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
            val logoCanvas = android.graphics.Canvas(logoBitmap)
            logoDrawable.setBounds(0, 0, logoSize, logoSize)
            logoDrawable.draw(logoCanvas)

            val logoRect = RectF(MARGIN_X, 22f, MARGIN_X + logoSize, 22f + logoSize)
            val clipPath = Path().apply {
                addRoundRect(logoRect, 10f, 10f, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(clipPath)
            canvas.drawBitmap(logoBitmap, MARGIN_X, 22f, null)
            canvas.restore()
        }

        textPaint.apply {
            textSize = 17f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.WHITE
        }
        canvas.drawText("ARVEXA STOPWATCH", MARGIN_X + 58f, 44f, textPaint)

        textPaint.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF00C2FF.toInt()
        }
        canvas.drawText(subtitleText, MARGIN_X + 58f, 59f, textPaint)

        val pillRight = PAGE_WIDTH - MARGIN_X
        val pillLeft = pillRight - 150f
        val pillRect = RectF(pillLeft, 32f, pillRight, 60f)

        fillPaint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(pillRect, 8f, 8f, fillPaint)

        val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = 0x3300C2FF.toInt()
        }
        canvas.drawRoundRect(pillRect, 8f, 8f, pillBorderPaint)

        textPaint.apply {
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF94A3B8.toInt()
        }
        canvas.drawText("EXPORT DATE", pillLeft + 12f, 43f, textPaint)

        textPaint.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.WHITE
        }
        canvas.drawText(dateStr, pillLeft + 12f, 54f, textPaint)
    }

    private fun drawCompactHeader(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        textPaint: Paint,
        pageNum: Int,
        totalPages: Int,
        dateStr: String,
        titleText: String
    ) {
        fillPaint.color = 0xFF0F172A.toInt()
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 42f, fillPaint)

        fillPaint.shader = LinearGradient(
            0f, 42f, PAGE_WIDTH.toFloat(), 42f,
            intArrayOf(0xFF00C2FF.toInt(), 0xFF6C5CE7.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 42f, PAGE_WIDTH.toFloat(), 44f, fillPaint)
        fillPaint.shader = null

        textPaint.apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.WHITE
        }
        canvas.drawText(titleText, MARGIN_X, 26f, textPaint)

        textPaint.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = 0xFF94A3B8.toInt()
        }
        canvas.drawText("$dateStr  |  Page $pageNum of $totalPages", PAGE_WIDTH - MARGIN_X - 160f, 26f, textPaint)
    }

    // ─────────────────────────────────────────────────────────────
    // KPI CARDS DRAWING METHODS
    // ─────────────────────────────────────────────────────────────
    private fun drawKpiSummaryCards(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        startY: Float,
        totalElapsedMillis: Long,
        lapCount: Int,
        bestLap: Lap?,
        avgLapMillis: Long,
        precisionMode: PrecisionMode
    ) {
        val availableWidth = PAGE_WIDTH - (MARGIN_X * 2)
        val cardGap = 8f
        val cardWidth = (availableWidth - (cardGap * 3)) / 4f
        val cardHeight = 62f

        val totalTimeStr = TimeFormatter.format(totalElapsedMillis, precisionMode)
        val bestLapStr = bestLap?.let { TimeFormatter.format(it.lapTimeMillis, precisionMode) } ?: "--:--"
        val avgLapStr = if (avgLapMillis > 0) TimeFormatter.format(avgLapMillis, precisionMode) else "--:--"

        data class CardData(val title: String, val value: String, val subtext: String, val accentColor: Int)

        val cards = listOf(
            CardData("TOTAL TIME", totalTimeStr, "Elapsed", 0xFF00C2FF.toInt()),
            CardData("TOTAL LAPS", "$lapCount Laps", "Recorded", 0xFF00E5A8.toInt()),
            CardData("BEST LAP", bestLapStr, bestLap?.let { "Lap #${it.lapNumber}" } ?: "--", 0xFFFFB800.toInt()),
            CardData("AVG SPLIT", avgLapStr, "Average", 0xFF6C5CE7.toInt())
        )

        for (i in cards.indices) {
            val card = cards[i]
            val cardX = MARGIN_X + (i * (cardWidth + cardGap))
            drawSingleCard(canvas, fillPaint, strokePaint, textPaint, cardX, startY, cardWidth, cardHeight, card.title, card.value, card.subtext, card.accentColor)
        }
    }

    private fun drawWorkoutHistoryKpis(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        startY: Float,
        sessionCount: Int,
        totalDurationMillis: Long,
        totalLaps: Int,
        bestLapMillis: Long,
        precisionMode: PrecisionMode
    ) {
        val availableWidth = PAGE_WIDTH - (MARGIN_X * 2)
        val cardGap = 8f
        val cardWidth = (availableWidth - (cardGap * 3)) / 4f
        val cardHeight = 62f

        val totalTimeStr = TimeFormatter.format(totalDurationMillis, precisionMode)
        val bestLapStr = if (bestLapMillis > 0) TimeFormatter.format(bestLapMillis, precisionMode) else "--:--"

        data class CardData(val title: String, val value: String, val subtext: String, val accentColor: Int)

        val cards = listOf(
            CardData("SESSIONS", "$sessionCount Workouts", "Recorded Log", 0xFF00C2FF.toInt()),
            CardData("TOTAL TIME", totalTimeStr, "All Workouts", 0xFF00E5A8.toInt()),
            CardData("TOTAL LAPS", "$totalLaps Laps", "Completed", 0xFF6C5CE7.toInt()),
            CardData("BEST LAP", bestLapStr, "Fastest Split", 0xFFFFB800.toInt())
        )

        for (i in cards.indices) {
            val card = cards[i]
            val cardX = MARGIN_X + (i * (cardWidth + cardGap))
            drawSingleCard(canvas, fillPaint, strokePaint, textPaint, cardX, startY, cardWidth, cardHeight, card.title, card.value, card.subtext, card.accentColor)
        }
    }

    private fun drawSingleSessionKpis(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        startY: Float,
        session: WorkoutSession,
        precisionMode: PrecisionMode
    ) {
        val availableWidth = PAGE_WIDTH - (MARGIN_X * 2)
        val cardGap = 8f
        val cardWidth = (availableWidth - (cardGap * 3)) / 4f
        val cardHeight = 62f

        val durationStr = TimeFormatter.format(session.durationMillis, precisionMode)
        val bestLapStr = if (session.bestLapMillis > 0) TimeFormatter.format(session.bestLapMillis, precisionMode) else "--:--"
        val avgLapStr = if (session.avgLapMillis > 0) TimeFormatter.format(session.avgLapMillis, precisionMode) else "--:--"

        data class CardData(val title: String, val value: String, val subtext: String, val accentColor: Int)

        val cards = listOf(
            CardData("DURATION", durationStr, "Total Time", 0xFF00C2FF.toInt()),
            CardData("LAPS", "${session.lapCount} Laps", "Recorded", 0xFF00E5A8.toInt()),
            CardData("BEST LAP", bestLapStr, "Fastest Lap", 0xFFFFB800.toInt()),
            CardData("AVG SPLIT", avgLapStr, "Average", 0xFF6C5CE7.toInt())
        )

        for (i in cards.indices) {
            val card = cards[i]
            val cardX = MARGIN_X + (i * (cardWidth + cardGap))
            drawSingleCard(canvas, fillPaint, strokePaint, textPaint, cardX, startY, cardWidth, cardHeight, card.title, card.value, card.subtext, card.accentColor)
        }
    }

    private fun drawSingleCard(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        cardX: Float,
        startY: Float,
        cardWidth: Float,
        cardHeight: Float,
        title: String,
        value: String,
        subtext: String,
        accentColor: Int
    ) {
        val cardRect = RectF(cardX, startY, cardX + cardWidth, startY + cardHeight)

        fillPaint.color = 0xFFF8FAFC.toInt()
        canvas.drawRoundRect(cardRect, 8f, 8f, fillPaint)

        strokePaint.apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 1f
        }
        canvas.drawRoundRect(cardRect, 8f, 8f, strokePaint)

        fillPaint.color = accentColor
        val stripRect = RectF(cardX, startY, cardX + cardWidth, startY + 3.5f)
        val stripPath = Path().apply {
            addRoundRect(stripRect, floatArrayOf(8f, 8f, 8f, 8f, 0f, 0f, 0f, 0f), Path.Direction.CW)
        }
        canvas.drawPath(stripPath, fillPaint)

        textPaint.apply {
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF64748B.toInt()
        }
        canvas.drawText(title, cardX + 10f, startY + 18f, textPaint)

        textPaint.apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF0F172A.toInt()
        }
        canvas.drawText(value, cardX + 10f, startY + 38f, textPaint)

        textPaint.apply {
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = accentColor
        }
        canvas.drawText(subtext, cardX + 10f, startY + 52f, textPaint)
    }

    // ─────────────────────────────────────────────────────────────
    // TABLE: LAPS TABLE
    // ─────────────────────────────────────────────────────────────
    private fun drawTableHeader(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        textPaint: Paint,
        y: Float
    ) {
        val headerRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 22f)
        fillPaint.color = 0xFF0F172A.toInt()
        canvas.drawRoundRect(headerRect, 6f, 6f, fillPaint)

        textPaint.apply {
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.WHITE
        }

        val baselineY = y + 14.5f
        canvas.drawText("LAP", MARGIN_X + 14f, baselineY, textPaint)
        canvas.drawText("SPLIT TIME", MARGIN_X + 75f, baselineY, textPaint)
        canvas.drawText("TOTAL TIME", MARGIN_X + 195f, baselineY, textPaint)
        canvas.drawText("DIFFERENCE", MARGIN_X + 315f, baselineY, textPaint)
        canvas.drawText("STATUS", MARGIN_X + 435f, baselineY, textPaint)
    }

    private fun drawTableRow(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        y: Float,
        rowHeight: Float,
        lap: Lap,
        isAlternate: Boolean,
        isBest: Boolean,
        isSlowest: Boolean,
        precisionMode: PrecisionMode
    ) {
        val rowRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight)
        fillPaint.color = if (isAlternate) 0xFFF8FAFC.toInt() else Color.WHITE
        canvas.drawRect(rowRect, fillPaint)

        strokePaint.apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 0.6f
        }
        canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, strokePaint)

        val baselineY = y + 14.5f

        val badgeX = MARGIN_X + 14f
        val badgeY = y + 4f
        val badgeRect = RectF(badgeX, badgeY, badgeX + 26f, badgeY + 14f)
        fillPaint.color = if (isBest) 0xFFFEF3C7.toInt() else if (isSlowest) 0xFFFFE4E6.toInt() else 0xFFE2E8F0.toInt()
        canvas.drawRoundRect(badgeRect, 4f, 4f, fillPaint)

        textPaint.apply {
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = if (isBest) 0xFFB45309.toInt() else if (isSlowest) 0xFFBE123C.toInt() else 0xFF1E293B.toInt()
        }
        canvas.drawText("#%02d".format(lap.lapNumber), badgeX + 4f, baselineY - 1f, textPaint)

        textPaint.apply {
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF0F172A.toInt()
        }
        val splitStr = TimeFormatter.format(lap.lapTimeMillis, precisionMode)
        canvas.drawText(splitStr, MARGIN_X + 75f, baselineY, textPaint)

        textPaint.apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = 0xFF475569.toInt()
        }
        val totalStr = TimeFormatter.format(lap.totalTimeMillis, precisionMode)
        canvas.drawText(totalStr, MARGIN_X + 195f, baselineY, textPaint)

        val diff = lap.diffFromPreviousMillis
        if (lap.lapNumber == 1) {
            textPaint.apply {
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                color = 0xFF94A3B8.toInt()
            }
            canvas.drawText("Base Split", MARGIN_X + 315f, baselineY, textPaint)
        } else if (diff < 0) {
            textPaint.apply {
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                color = 0xFF059669.toInt()
            }
            val diffStr = "-${TimeFormatter.format(abs(diff), precisionMode)}"
            canvas.drawText(diffStr, MARGIN_X + 315f, baselineY, textPaint)
        } else if (diff > 0) {
            textPaint.apply {
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                color = 0xFFE11D48.toInt()
            }
            val diffStr = "+${TimeFormatter.format(abs(diff), precisionMode)}"
            canvas.drawText(diffStr, MARGIN_X + 315f, baselineY, textPaint)
        } else {
            textPaint.apply {
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                color = 0xFF64748B.toInt()
            }
            canvas.drawText("±0.00", MARGIN_X + 315f, baselineY, textPaint)
        }

        if (isBest) {
            val pill = RectF(MARGIN_X + 435f, y + 4f, MARGIN_X + 495f, y + 17f)
            fillPaint.color = 0xFFFEF3C7.toInt()
            canvas.drawRoundRect(pill, 4f, 4f, fillPaint)
            textPaint.apply {
                textSize = 7.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                color = 0xFFB45309.toInt()
            }
            canvas.drawText("BEST LAP ★", MARGIN_X + 441f, baselineY - 2f, textPaint)
        } else if (isSlowest) {
            val pill = RectF(MARGIN_X + 435f, y + 4f, MARGIN_X + 490f, y + 17f)
            fillPaint.color = 0xFFFFE4E6.toInt()
            canvas.drawRoundRect(pill, 4f, 4f, fillPaint)
            textPaint.apply {
                textSize = 7.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                color = 0xFFBE123C.toInt()
            }
            canvas.drawText("SLOWEST", MARGIN_X + 441f, baselineY - 2f, textPaint)
        } else {
            textPaint.apply {
                textSize = 8f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                color = 0xFF94A3B8.toInt()
            }
            canvas.drawText("Normal", MARGIN_X + 435f, baselineY, textPaint)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TABLE: WORKOUT HISTORY TABLE
    // ─────────────────────────────────────────────────────────────
    private fun drawWorkoutTableHeader(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        textPaint: Paint,
        y: Float
    ) {
        val headerRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 22f)
        fillPaint.color = 0xFF0F172A.toInt()
        canvas.drawRoundRect(headerRect, 6f, 6f, fillPaint)

        textPaint.apply {
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.WHITE
        }

        val baselineY = y + 14.5f
        canvas.drawText("#", MARGIN_X + 14f, baselineY, textPaint)
        canvas.drawText("WORKOUT SESSION / DATE", MARGIN_X + 45f, baselineY, textPaint)
        canvas.drawText("DURATION", MARGIN_X + 235f, baselineY, textPaint)
        canvas.drawText("LAPS", MARGIN_X + 335f, baselineY, textPaint)
        canvas.drawText("BEST LAP", MARGIN_X + 405f, baselineY, textPaint)
        canvas.drawText("AVG SPLIT", MARGIN_X + 480f, baselineY, textPaint)
    }

    private fun drawWorkoutTableRow(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        y: Float,
        rowHeight: Float,
        index: Int,
        session: WorkoutSession,
        isAlternate: Boolean,
        precisionMode: PrecisionMode
    ) {
        val rowRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight)
        fillPaint.color = if (isAlternate) 0xFFF8FAFC.toInt() else Color.WHITE
        canvas.drawRect(rowRect, fillPaint)

        strokePaint.apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 0.6f
        }
        canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, strokePaint)

        // Index
        textPaint.apply {
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF64748B.toInt()
        }
        canvas.drawText("%02d".format(index), MARGIN_X + 14f, y + 16f, textPaint)

        // Title and Date
        textPaint.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF0F172A.toInt()
        }
        val safeTitle = if (session.title.length > 28) session.title.take(25) + "..." else session.title
        canvas.drawText(safeTitle, MARGIN_X + 45f, y + 12f, textPaint)

        textPaint.apply {
            textSize = 7f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = 0xFF64748B.toInt()
        }
        val dateStr = TimeFormatter.formatDate(session.timestamp)
        canvas.drawText(dateStr, MARGIN_X + 45f, y + 22f, textPaint)

        // Duration
        textPaint.apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = 0xFF059669.toInt()
        }
        val durationStr = TimeFormatter.format(session.durationMillis, precisionMode)
        canvas.drawText(durationStr, MARGIN_X + 235f, y + 16f, textPaint)

        // Laps
        textPaint.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = 0xFF0284C7.toInt()
        }
        canvas.drawText("${session.lapCount} Laps", MARGIN_X + 335f, y + 16f, textPaint)

        // Best Lap
        textPaint.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = if (session.bestLapMillis > 0) 0xFFD97706.toInt() else 0xFF94A3B8.toInt()
        }
        val bestStr = if (session.bestLapMillis > 0) TimeFormatter.format(session.bestLapMillis, precisionMode) else "--"
        canvas.drawText(bestStr, MARGIN_X + 405f, y + 16f, textPaint)

        // Avg Split
        textPaint.apply {
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = if (session.avgLapMillis > 0) 0xFF6C5CE7.toInt() else 0xFF94A3B8.toInt()
        }
        val avgStr = if (session.avgLapMillis > 0) TimeFormatter.format(session.avgLapMillis, precisionMode) else "--"
        canvas.drawText(avgStr, MARGIN_X + 480f, y + 16f, textPaint)
    }

    // ─────────────────────────────────────────────────────────────
    // TABLE: SINGLE SESSION TABLE
    // ─────────────────────────────────────────────────────────────
    private fun drawSingleSessionTableHeader(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        textPaint: Paint,
        y: Float
    ) {
        val headerRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + 22f)
        fillPaint.color = 0xFF0F172A.toInt()
        canvas.drawRoundRect(headerRect, 6f, 6f, fillPaint)

        textPaint.apply {
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = Color.WHITE
        }

        val baselineY = y + 14.5f
        canvas.drawText("RECORDED LAP SPLIT", MARGIN_X + 18f, baselineY, textPaint)
    }

    private fun drawSingleSessionTableRow(
        canvas: android.graphics.Canvas,
        fillPaint: Paint,
        strokePaint: Paint,
        textPaint: Paint,
        y: Float,
        rowHeight: Float,
        lapSummaryLine: String,
        isAlternate: Boolean
    ) {
        val rowRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowHeight)
        fillPaint.color = if (isAlternate) 0xFFF8FAFC.toInt() else Color.WHITE
        canvas.drawRect(rowRect, fillPaint)

        strokePaint.apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 0.6f
        }
        canvas.drawLine(MARGIN_X, y + rowHeight, PAGE_WIDTH - MARGIN_X, y + rowHeight, strokePaint)

        textPaint.apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = 0xFF0F172A.toInt()
        }
        canvas.drawText(lapSummaryLine, MARGIN_X + 18f, y + 14.5f, textPaint)
    }

    // ─────────────────────────────────────────────────────────────
    // FOOTER
    // ─────────────────────────────────────────────────────────────
    private fun drawPageFooter(
        canvas: android.graphics.Canvas,
        strokePaint: Paint,
        textPaint: Paint,
        pageNum: Int,
        totalPages: Int
    ) {
        val footerY = 810f

        strokePaint.apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN_X, footerY, PAGE_WIDTH - MARGIN_X, footerY, strokePaint)

        textPaint.apply {
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = 0xFF94A3B8.toInt()
        }
        canvas.drawText("Generated by Arvexa Stopwatch Pro • High Precision Timing System", MARGIN_X, footerY + 14f, textPaint)

        val pageStr = "Page $pageNum of $totalPages"
        canvas.drawText(pageStr, PAGE_WIDTH - MARGIN_X - 60f, footerY + 14f, textPaint)
    }

    // ─────────────────────────────────────────────────────────────
    // UTILS
    // ─────────────────────────────────────────────────────────────
    private fun savePdfToCache(context: Context, pdfDocument: PdfDocument, fileName: String): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun sharePdfFile(
        context: Context,
        pdfFile: File,
        subject: String = "Arvexa Stopwatch Report",
        message: String = "Here is the official Arvexa Stopwatch report attached as a PDF."
    ) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Report (PDF)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
