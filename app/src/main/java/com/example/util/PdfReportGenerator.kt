package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.FinancialSummary
import com.example.data.model.LOAN_TYPE_LENT
import com.example.data.model.LoanEntity
import com.example.data.model.TYPE_REFUND
import com.example.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN_X = 36f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_BOTTOM = 44f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2) // 523f

    private const val ROW_HEIGHT = 28f // Comfortable row height for 2-line date/time
    private const val TABLE_HEADER_HEIGHT = 26f
    private const val SECTION_HEADER_HEIGHT = 30f
    private const val SUBTOTAL_ROW_HEIGHT = 24f
    private const val SUMMARY_CARD_HEIGHT = 92f

    fun generateReport(
        context: Context,
        yearMonth: String,
        summary: FinancialSummary,
        transactions: List<TransactionEntity>,
        accountName: String = "আমার হাতখরচ",
        loans: List<LoanEntity> = emptyList()
    ): File {
        val pdfDocument = PdfDocument()

        // Load Hind Siliguri Bold typeface
        val bengaliBoldTypeface = try {
            Typeface.createFromAsset(context.assets, "fonts/hind_siliguri_bold.ttf")
        } catch (e: Exception) {
            try {
                Typeface.createFromAsset(context.assets, "fonts/hind_siliguri.ttf")
            } catch (e2: Exception) {
                try {
                    Typeface.createFromAsset(context.assets, "fonts/noto_sans_bengali.ttf")
                } catch (e3: Exception) {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
            }
        }

        val bengaliRegularTypeface = try {
            Typeface.createFromAsset(context.assets, "fonts/hind_siliguri.ttf")
        } catch (e: Exception) {
            bengaliBoldTypeface
        }

        // Paints adhering to the aesthetic theme
        val titlePaint = Paint().apply {
            typeface = bengaliBoldTypeface
            textSize = 19f
            color = Color.rgb(28, 43, 51) // #1C2B33 Obsidian
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            typeface = bengaliBoldTypeface
            textSize = 10f
            color = Color.rgb(90, 110, 124) // #5A6E7C Muted Slate
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            typeface = bengaliRegularTypeface
            textSize = 9.5f
            color = Color.rgb(28, 43, 51)
            isAntiAlias = true
        }

        val timePaint = Paint().apply {
            typeface = bengaliRegularTypeface
            textSize = 8f
            color = Color.rgb(115, 130, 145) // Subtle gray for time
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            typeface = bengaliBoldTypeface
            textSize = 9.5f
            color = Color.rgb(28, 43, 51)
            isAntiAlias = true
        }

        val expenseTextPaint = Paint().apply {
            typeface = bengaliBoldTypeface
            textSize = 9.5f
            color = Color.rgb(224, 49, 49) // #E03131 Coral Red
            isAntiAlias = true
        }

        val incomeTextPaint = Paint().apply {
            typeface = bengaliBoldTypeface
            textSize = 9.5f
            color = Color.rgb(12, 166, 120) // #0CA678 Emerald Green
            isAntiAlias = true
        }

        val bgPaint = Paint().apply {
            isAntiAlias = true
        }

        val gridBorderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240) // #E2E8F0 Subtle table grid border
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val subtleColLinePaint = Paint().apply {
            color = Color.rgb(238, 242, 246) // #EEF2F6 Very subtle column divider
            strokeWidth = 0.6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val footerPaint = Paint().apply {
            typeface = bengaliRegularTypeface
            textSize = 8.5f
            color = Color.rgb(130, 145, 155)
            isAntiAlias = true
        }

        // Data partitioning
        val expenseList = transactions.filter { it.type != TYPE_REFUND }
        val incomeList = transactions.filter { it.type == TYPE_REFUND }
        val totalExpenses = expenseList.sumOf { it.amount }
        val totalIncome = incomeList.sumOf { it.amount }
        val totalLent = loans.filter { it.type == LOAN_TYPE_LENT && !it.isSettled }.sumOf { it.remainingAmount }
        val totalBorrowed = loans.filter { it.type != LOAN_TYPE_LENT && !it.isSettled }.sumOf { it.remainingAmount }

        val totalPages = simulatePageCount(expenseList.size, incomeList.size, loans.size)

        var currentPageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var currentY = MARGIN_TOP

        fun advanceToNextPage(headerTitle: String? = null) {
            drawPageFooter(canvas, footerPaint, currentPageNum, totalPages)
            pdfDocument.finishPage(page)

            currentPageNum++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNum).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            currentY = MARGIN_TOP

            // Continuation Banner
            if (headerTitle != null) {
                val contRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + 22f)
                bgPaint.color = Color.rgb(241, 245, 248)
                canvas.drawRoundRect(contRect, 4f, 4f, bgPaint)
                canvas.drawText("$headerTitle (চলমান)", MARGIN_X + 10f, currentY + 15f, subtitlePaint)
                currentY += 28f
            }
        }

        // =========================================================================
        // COVER HEADER (Page 1)
        // =========================================================================
        // App Title & Brand
        canvas.drawText("আমার হিসাব • মাসিক আর্থিক প্রতিবেদন", MARGIN_X, currentY + 20f, titlePaint)

        // Account Name Badge
        val accBadgeWidth = subtitlePaint.measureText("খাতা: $accountName") + 16f
        val accBadgeRect = RectF(MARGIN_X + CONTENT_WIDTH - accBadgeWidth, currentY + 4f, MARGIN_X + CONTENT_WIDTH, currentY + 24f)
        bgPaint.color = Color.rgb(234, 244, 254) // Soft primary tint
        canvas.drawRoundRect(accBadgeRect, 10f, 10f, bgPaint)

        val accBadgeTextPaint = Paint(subtitlePaint).apply {
            color = Color.rgb(0, 100, 224)
        }
        canvas.drawText("খাতা: $accountName", accBadgeRect.left + 8f, accBadgeRect.bottom - 6f, accBadgeTextPaint)

        val monthBangla = DateUtils.formatYearMonthBengali(yearMonth)
        val generatedAtBangla = DateUtils.formatDateBengali(System.currentTimeMillis())
        canvas.drawText("হিসাবের মাস: $monthBangla  |  রিপোর্ট তৈরির তারিখ: $generatedAtBangla", MARGIN_X, currentY + 36f, subtitlePaint)

        currentY += 46f

        // Top decorative accent line
        bgPaint.color = Color.rgb(0, 130, 251) // Electric Blue
        canvas.drawRoundRect(RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + 2.5f), 1f, 1f, bgPaint)
        currentY += 12f

        // =========================================================================
        // 3 FINANCIAL SUMMARY CARDS
        // =========================================================================
        val cardWidth = (CONTENT_WIDTH - 20f) / 3f
        val cardHeight = SUMMARY_CARD_HEIGHT

        // Card 1: Total Received / Income
        val card1Rect = RectF(MARGIN_X, currentY, MARGIN_X + cardWidth, currentY + cardHeight)
        bgPaint.color = Color.rgb(241, 249, 246)
        canvas.drawRoundRect(card1Rect, 12f, 12f, bgPaint)
        bgPaint.color = Color.rgb(12, 166, 120) // Green border
        canvas.drawRoundRect(RectF(card1Rect.left, card1Rect.top, card1Rect.left + 4f, card1Rect.bottom), 4f, 4f, bgPaint)

        var box1X = card1Rect.left + 12f
        var boxY = card1Rect.top + 22f
        canvas.drawText("টাকা পেয়েছি (+)", box1X, boxY, subtitlePaint)
        boxY += 24f
        val largeIncPaint = Paint(incomeTextPaint).apply { textSize = 17f }
        canvas.drawText(DateUtils.formatTaka(totalIncome), box1X, boxY, largeIncPaint)
        boxY += 18f
        canvas.drawText("মোট জমা: ${DateUtils.toBengaliDigits(incomeList.size)} টি", box1X, boxY, textPaint)

        // Card 2: Total Spent / Expenses
        val card2Rect = RectF(MARGIN_X + cardWidth + 10f, currentY, MARGIN_X + (cardWidth * 2) + 10f, currentY + cardHeight)
        bgPaint.color = Color.rgb(255, 245, 245)
        canvas.drawRoundRect(card2Rect, 12f, 12f, bgPaint)
        bgPaint.color = Color.rgb(224, 49, 49) // Red border
        canvas.drawRoundRect(RectF(card2Rect.left, card2Rect.top, card2Rect.left + 4f, card2Rect.bottom), 4f, 4f, bgPaint)

        var box2X = card2Rect.left + 12f
        boxY = card2Rect.top + 22f
        canvas.drawText("খরচ করেছি (-)", box2X, boxY, subtitlePaint)
        boxY += 24f
        val largeExpPaint = Paint(expenseTextPaint).apply { textSize = 17f }
        canvas.drawText(DateUtils.formatTaka(totalExpenses), box2X, boxY, largeExpPaint)
        boxY += 18f
        canvas.drawText("মোট খরচ: ${DateUtils.toBengaliDigits(expenseList.size)} টি", box2X, boxY, textPaint)

        // Card 3: In Hand Balance
        val card3Rect = RectF(MARGIN_X + (cardWidth * 2) + 20f, currentY, MARGIN_X + CONTENT_WIDTH, currentY + cardHeight)
        val balColor = if (summary.actualRemainingBalance >= 0L) Color.rgb(0, 100, 224) else Color.rgb(224, 49, 49)
        bgPaint.color = Color.rgb(243, 247, 252)
        canvas.drawRoundRect(card3Rect, 12f, 12f, bgPaint)
        bgPaint.color = balColor
        canvas.drawRoundRect(RectF(card3Rect.left, card3Rect.top, card3Rect.left + 4f, card3Rect.bottom), 4f, 4f, bgPaint)

        var box3X = card3Rect.left + 12f
        boxY = card3Rect.top + 22f
        canvas.drawText("হাতে আছে (ব্যালেন্স)", box3X, boxY, subtitlePaint)
        boxY += 24f
        val balVal = DateUtils.formatTaka(summary.actualRemainingBalance)
        val largeBalPaint = Paint(boldTextPaint).apply {
            textSize = 17f
            color = balColor
        }
        canvas.drawText(balVal, box3X, boxY, largeBalPaint)
        boxY += 18f
        canvas.drawText("বাজেট ব্যবহার: ${DateUtils.toBengaliDigits(summary.budgetUsagePercent)}%", box3X, boxY, textPaint)

        currentY += SUMMARY_CARD_HEIGHT + 16f

        // =========================================================================
        // TABLE COLUMN COORDINATES & GRID HELPERS
        // =========================================================================
        // Col boundaries:
        // Sl: MARGIN_X to MARGIN_X + 32f
        // Date: MARGIN_X + 32f to MARGIN_X + 130f (width 98f)
        // Desc: MARGIN_X + 130f to MARGIN_X + 314f (width 184f)
        // Cat:  MARGIN_X + 314f to MARGIN_X + 414f (width 100f)
        // Amt:  MARGIN_X + 414f to MARGIN_X + CONTENT_WIDTH (width 109f)

        val splitSl = MARGIN_X + 32f
        val splitDate = MARGIN_X + 130f
        val splitDesc = MARGIN_X + 314f
        val splitCat = MARGIN_X + 414f

        val colSl = MARGIN_X + 6f
        val colDate = splitSl + 6f
        val colDesc = splitDate + 6f
        val colCat = splitDesc + 6f
        val colAmount = MARGIN_X + CONTENT_WIDTH - 8f

        fun drawTableHeader(headerBgColor: Int, descLabel: String, amountLabel: String) {
            val hRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + TABLE_HEADER_HEIGHT)
            bgPaint.color = headerBgColor
            canvas.drawRoundRect(hRect, 4f, 4f, bgPaint)

            val hTextPaint = Paint().apply {
                typeface = bengaliBoldTypeface
                textSize = 9.5f
                color = Color.WHITE
                isAntiAlias = true
            }

            canvas.drawText("নং", colSl, currentY + 17f, hTextPaint)
            canvas.drawText("তারিখ ও সময়", colDate, currentY + 17f, hTextPaint)
            canvas.drawText(descLabel, colDesc, currentY + 17f, hTextPaint)
            canvas.drawText("ক্যাটাগরি", colCat, currentY + 17f, hTextPaint)

            val amtWidth = hTextPaint.measureText(amountLabel)
            canvas.drawText(amountLabel, colAmount - amtWidth, currentY + 17f, hTextPaint)

            // Header grid lines
            canvas.drawLine(splitSl, currentY, splitSl, currentY + TABLE_HEADER_HEIGHT, subtleColLinePaint)
            canvas.drawLine(splitDate, currentY, splitDate, currentY + TABLE_HEADER_HEIGHT, subtleColLinePaint)
            canvas.drawLine(splitDesc, currentY, splitDesc, currentY + TABLE_HEADER_HEIGHT, subtleColLinePaint)
            canvas.drawLine(splitCat, currentY, splitCat, currentY + TABLE_HEADER_HEIGHT, subtleColLinePaint)

            currentY += TABLE_HEADER_HEIGHT
        }

        // =========================================================================
        // SECTION 1: টাকা খরচ করেছি (EXPENSES)
        // =========================================================================
        if (currentY + SECTION_HEADER_HEIGHT + TABLE_HEADER_HEIGHT + (ROW_HEIGHT * 2) > PAGE_HEIGHT - MARGIN_BOTTOM) {
            advanceToNextPage()
        }

        // Section Title Banner for Expenses
        val expBannerRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + SECTION_HEADER_HEIGHT)
        bgPaint.color = Color.rgb(255, 236, 235) // #FFEAEA
        canvas.drawRoundRect(expBannerRect, 6f, 6f, bgPaint)

        // Left accent bar
        bgPaint.color = Color.rgb(224, 49, 49) // #E03131
        canvas.drawRoundRect(RectF(MARGIN_X, currentY, MARGIN_X + 5f, currentY + SECTION_HEADER_HEIGHT), 2f, 2f, bgPaint)

        val expTitleText = "🔴 টাকা খরচ করেছি (${DateUtils.toBengaliDigits(expenseList.size)} টি • মোট: ${DateUtils.formatTaka(totalExpenses)})"
        val secTitlePaintExp = Paint(boldTextPaint).apply {
            color = Color.rgb(180, 25, 25)
            textSize = 11.5f
        }
        canvas.drawText(expTitleText, MARGIN_X + 12f, currentY + 20f, secTitlePaintExp)
        currentY += SECTION_HEADER_HEIGHT + 6f

        drawTableHeader(
            headerBgColor = Color.rgb(28, 43, 51), // Obsidian header
            descLabel = "খরচের বিবরণ",
            amountLabel = "পরিমাণ (-)"
        )

        val expTableStartY = currentY

        if (expenseList.isEmpty()) {
            val emptyRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + 26f)
            bgPaint.color = Color.rgb(248, 250, 252)
            canvas.drawRect(emptyRect, bgPaint)
            canvas.drawText("এই মাসে কোনো খরচের হিসাব লেখা হয়নি।", MARGIN_X + 16f, currentY + 17f, subtitlePaint)
            canvas.drawLine(MARGIN_X, currentY + 26f, MARGIN_X + CONTENT_WIDTH, currentY + 26f, gridBorderPaint)
            currentY += 26f
        } else {
            expenseList.forEachIndexed { idx, tx ->
                if (currentY + ROW_HEIGHT + SUBTOTAL_ROW_HEIGHT > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    advanceToNextPage("খরচের তালিকা (চলমান)")
                    drawTableHeader(Color.rgb(28, 43, 51), "খরচের বিবরণ", "পরিমাণ (-)")
                }

                val rowTop = currentY
                val rowRect = RectF(MARGIN_X, rowTop, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT)

                // Soft alternating zebra background
                if (idx % 2 == 1) {
                    bgPaint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(rowRect, bgPaint)
                }

                // Vertical column grid lines
                canvas.drawLine(splitSl, rowTop, splitSl, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(splitDate, rowTop, splitDate, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(splitDesc, rowTop, splitDesc, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(splitCat, rowTop, splitCat, rowTop + ROW_HEIGHT, subtleColLinePaint)

                // Horizontal bottom row divider line
                canvas.drawLine(MARGIN_X, rowTop + ROW_HEIGHT, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT, gridBorderPaint)

                // Outer left and right borders
                canvas.drawLine(MARGIN_X, rowTop, MARGIN_X, rowTop + ROW_HEIGHT, gridBorderPaint)
                canvas.drawLine(MARGIN_X + CONTENT_WIDTH, rowTop, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT, gridBorderPaint)

                // Serial No
                canvas.drawText(DateUtils.toBengaliDigits(idx + 1), colSl, rowTop + 17f, textPaint)

                // Date & Time in TWO SEPARATE LINES to prevent overlapping
                val dateOnlyStr = DateUtils.formatDateBengali(tx.timestamp).take(14)
                val timeOnlyStr = DateUtils.formatTimeBengali(tx.timestamp)
                canvas.drawText(dateOnlyStr, colDate, rowTop + 13f, boldTextPaint)
                canvas.drawText(timeOnlyStr, colDate, rowTop + 23f, timePaint)

                // Description
                val descStr = if (tx.description.length > 26) tx.description.take(25) + "…" else tx.description
                canvas.drawText(descStr, colDesc, rowTop + 17f, boldTextPaint)

                // Category
                val catStr = if (tx.category.length > 15) tx.category.take(14) + "…" else tx.category
                canvas.drawText(catStr, colCat, rowTop + 17f, textPaint)

                // Amount
                val amtStr = "- ${DateUtils.formatTaka(tx.amount)}"
                val amtWidth = expenseTextPaint.measureText(amtStr)
                canvas.drawText(amtStr, colAmount - amtWidth, rowTop + 17f, expenseTextPaint)

                currentY += ROW_HEIGHT
            }

            // Subtotal row for Expenses
            val subRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + SUBTOTAL_ROW_HEIGHT)
            bgPaint.color = Color.rgb(255, 241, 240)
            canvas.drawRect(subRect, bgPaint)

            canvas.drawLine(MARGIN_X, currentY + SUBTOTAL_ROW_HEIGHT, MARGIN_X + CONTENT_WIDTH, currentY + SUBTOTAL_ROW_HEIGHT, gridBorderPaint)
            canvas.drawLine(MARGIN_X, currentY, MARGIN_X, currentY + SUBTOTAL_ROW_HEIGHT, gridBorderPaint)
            canvas.drawLine(MARGIN_X + CONTENT_WIDTH, currentY, MARGIN_X + CONTENT_WIDTH, currentY + SUBTOTAL_ROW_HEIGHT, gridBorderPaint)

            canvas.drawText("মোট খরচ (Subtotal):", colDesc, currentY + 16f, boldTextPaint)
            val totalExpStr = "- ${DateUtils.formatTaka(totalExpenses)}"
            val expTotalWidth = expenseTextPaint.measureText(totalExpStr)
            canvas.drawText(totalExpStr, colAmount - expTotalWidth, currentY + 16f, expenseTextPaint)
            currentY += SUBTOTAL_ROW_HEIGHT
        }

        currentY += 18f

        // =========================================================================
        // SECTION 2: টাকা পেয়েছি (INCOME / DEPOSITS)
        // =========================================================================
        if (currentY + SECTION_HEADER_HEIGHT + TABLE_HEADER_HEIGHT + (ROW_HEIGHT * 2) > PAGE_HEIGHT - MARGIN_BOTTOM) {
            advanceToNextPage()
        }

        // Section Title Banner for Income
        val incBannerRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + SECTION_HEADER_HEIGHT)
        bgPaint.color = Color.rgb(230, 252, 245) // #E6FCF5
        canvas.drawRoundRect(incBannerRect, 6f, 6f, bgPaint)

        // Left accent bar
        bgPaint.color = Color.rgb(12, 166, 120) // #0CA678
        canvas.drawRoundRect(RectF(MARGIN_X, currentY, MARGIN_X + 5f, currentY + SECTION_HEADER_HEIGHT), 2f, 2f, bgPaint)

        val incTitleText = "🟢 টাকা পেয়েছি (${DateUtils.toBengaliDigits(incomeList.size)} টি • মোট: ${DateUtils.formatTaka(totalIncome)})"
        val secTitlePaintInc = Paint(boldTextPaint).apply {
            color = Color.rgb(10, 120, 85)
            textSize = 11.5f
        }
        canvas.drawText(incTitleText, MARGIN_X + 12f, currentY + 20f, secTitlePaintInc)
        currentY += SECTION_HEADER_HEIGHT + 6f

        drawTableHeader(
            headerBgColor = Color.rgb(12, 166, 120), // Emerald Green header
            descLabel = "জমার বিবরণ",
            amountLabel = "পরিমাণ (+)"
        )

        if (incomeList.isEmpty()) {
            val emptyRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + 26f)
            bgPaint.color = Color.rgb(248, 250, 252)
            canvas.drawRect(emptyRect, bgPaint)
            canvas.drawText("এই মাসে কোনো টাকা জমার হিসাব লেখা হয়নি।", MARGIN_X + 16f, currentY + 17f, subtitlePaint)
            canvas.drawLine(MARGIN_X, currentY + 26f, MARGIN_X + CONTENT_WIDTH, currentY + 26f, gridBorderPaint)
            currentY += 26f
        } else {
            incomeList.forEachIndexed { idx, tx ->
                if (currentY + ROW_HEIGHT + SUBTOTAL_ROW_HEIGHT > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    advanceToNextPage("জমার তালিকা (চলমান)")
                    drawTableHeader(Color.rgb(12, 166, 120), "জমার বিবরণ", "পরিমাণ (+)")
                }

                val rowTop = currentY
                val rowRect = RectF(MARGIN_X, rowTop, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT)

                // Soft alternating zebra background
                if (idx % 2 == 1) {
                    bgPaint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(rowRect, bgPaint)
                }

                // Vertical column grid lines
                canvas.drawLine(splitSl, rowTop, splitSl, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(splitDate, rowTop, splitDate, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(splitDesc, rowTop, splitDesc, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(splitCat, rowTop, splitCat, rowTop + ROW_HEIGHT, subtleColLinePaint)

                // Horizontal bottom row divider line
                canvas.drawLine(MARGIN_X, rowTop + ROW_HEIGHT, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT, gridBorderPaint)

                // Outer left and right borders
                canvas.drawLine(MARGIN_X, rowTop, MARGIN_X, rowTop + ROW_HEIGHT, gridBorderPaint)
                canvas.drawLine(MARGIN_X + CONTENT_WIDTH, rowTop, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT, gridBorderPaint)

                // Serial No
                canvas.drawText(DateUtils.toBengaliDigits(idx + 1), colSl, rowTop + 17f, textPaint)

                // Date & Time in TWO SEPARATE LINES to prevent overlapping
                val dateOnlyStr = DateUtils.formatDateBengali(tx.timestamp).take(14)
                val timeOnlyStr = DateUtils.formatTimeBengali(tx.timestamp)
                canvas.drawText(dateOnlyStr, colDate, rowTop + 13f, boldTextPaint)
                canvas.drawText(timeOnlyStr, colDate, rowTop + 23f, timePaint)

                // Description
                val descStr = if (tx.description.length > 26) tx.description.take(25) + "…" else tx.description
                canvas.drawText(descStr, colDesc, rowTop + 17f, boldTextPaint)

                // Category
                val catDisplay = if (tx.category.isBlank() || tx.category == "সাধারণ") "প্রাপ্তি / জমা" else tx.category
                val catStr = if (catDisplay.length > 15) catDisplay.take(14) + "…" else catDisplay
                canvas.drawText(catStr, colCat, rowTop + 17f, textPaint)

                // Amount
                val amtStr = "+ ${DateUtils.formatTaka(tx.amount)}"
                val amtWidth = incomeTextPaint.measureText(amtStr)
                canvas.drawText(amtStr, colAmount - amtWidth, rowTop + 17f, incomeTextPaint)

                currentY += ROW_HEIGHT
            }

            // Subtotal row for Income
            val subRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + SUBTOTAL_ROW_HEIGHT)
            bgPaint.color = Color.rgb(235, 252, 247)
            canvas.drawRect(subRect, bgPaint)

            canvas.drawLine(MARGIN_X, currentY + SUBTOTAL_ROW_HEIGHT, MARGIN_X + CONTENT_WIDTH, currentY + SUBTOTAL_ROW_HEIGHT, gridBorderPaint)
            canvas.drawLine(MARGIN_X, currentY, MARGIN_X, currentY + SUBTOTAL_ROW_HEIGHT, gridBorderPaint)
            canvas.drawLine(MARGIN_X + CONTENT_WIDTH, currentY, MARGIN_X + CONTENT_WIDTH, currentY + SUBTOTAL_ROW_HEIGHT, gridBorderPaint)

            canvas.drawText("মোট জমা (Subtotal):", colDesc, currentY + 16f, boldTextPaint)
            val totalIncStr = "+ ${DateUtils.formatTaka(totalIncome)}"
            val incTotalWidth = incomeTextPaint.measureText(totalIncStr)
            canvas.drawText(totalIncStr, colAmount - incTotalWidth, currentY + 16f, incomeTextPaint)
            currentY += SUBTOTAL_ROW_HEIGHT
        }

        // =========================================================================
        // SECTION 3: ধার-দেনা খাতা (DEBTS & LOANS SUMMARY)
        // =========================================================================
        if (loans.isNotEmpty()) {
            currentY += 18f

            if (currentY + SECTION_HEADER_HEIGHT + TABLE_HEADER_HEIGHT + (ROW_HEIGHT * 2) > PAGE_HEIGHT - MARGIN_BOTTOM) {
                advanceToNextPage()
            }

            // Section Title Banner for Loans
            val loanBannerRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + SECTION_HEADER_HEIGHT)
            bgPaint.color = Color.rgb(238, 242, 255) // #EEF2FF
            canvas.drawRoundRect(loanBannerRect, 6f, 6f, bgPaint)

            // Left accent bar
            bgPaint.color = Color.rgb(79, 70, 229) // Indigo
            canvas.drawRoundRect(RectF(MARGIN_X, currentY, MARGIN_X + 5f, currentY + SECTION_HEADER_HEIGHT), 2f, 2f, bgPaint)

            val loanTitleText = "🤝 ধার-দেনা খাতা (${DateUtils.toBengaliDigits(loans.size)} টি • মোট পাবো: ৳${totalLent} | মোট দেবো: ৳${totalBorrowed})"
            val secTitlePaintLoan = Paint(boldTextPaint).apply {
                color = Color.rgb(67, 56, 202)
                textSize = 11.5f
            }
            canvas.drawText(loanTitleText, MARGIN_X + 12f, currentY + 20f, secTitlePaintLoan)
            currentY += SECTION_HEADER_HEIGHT + 6f

            // Loan Table Column Splits:
            // Sl: MARGIN_X to MARGIN_X + 30f
            // Person: MARGIN_X + 30f to MARGIN_X + 155f (125f)
            // Type: MARGIN_X + 155f to MARGIN_X + 235f (80f)
            // Total: MARGIN_X + 235f to MARGIN_X + 315f (80f)
            // Paid: MARGIN_X + 315f to MARGIN_X + 395f (80f)
            // Due: MARGIN_X + 395f to MARGIN_X + 465f (70f)
            // Due Date: MARGIN_X + 465f to MARGIN_X + CONTENT_WIDTH (58f)

            val lSplitSl = MARGIN_X + 28f
            val lSplitPerson = MARGIN_X + 160f
            val lSplitType = MARGIN_X + 240f
            val lSplitTotal = MARGIN_X + 325f
            val lSplitPaid = MARGIN_X + 400f
            val lSplitDue = MARGIN_X + 460f

            // Loan Table Header
            val lhRect = RectF(MARGIN_X, currentY, MARGIN_X + CONTENT_WIDTH, currentY + TABLE_HEADER_HEIGHT)
            bgPaint.color = Color.rgb(79, 70, 229)
            canvas.drawRoundRect(lhRect, 4f, 4f, bgPaint)

            val lhTextPaint = Paint().apply {
                typeface = bengaliBoldTypeface
                textSize = 9.5f
                color = Color.WHITE
                isAntiAlias = true
            }

            canvas.drawText("নং", MARGIN_X + 6f, currentY + 17f, lhTextPaint)
            canvas.drawText("ব্যক্তি ও যোগাযোগ", lSplitSl + 6f, currentY + 17f, lhTextPaint)
            canvas.drawText("ধরণ", lSplitPerson + 6f, currentY + 17f, lhTextPaint)
            canvas.drawText("মোট ধার", lSplitType + 6f, currentY + 17f, lhTextPaint)
            canvas.drawText("পরিশোধ", lSplitTotal + 6f, currentY + 17f, lhTextPaint)
            canvas.drawText("বাকি", lSplitPaid + 6f, currentY + 17f, lhTextPaint)
            canvas.drawText("পরিশোধের তারিখ", lSplitDue + 6f, currentY + 17f, lhTextPaint)

            currentY += TABLE_HEADER_HEIGHT

            loans.forEachIndexed { idx, loan ->
                if (currentY + ROW_HEIGHT + SUBTOTAL_ROW_HEIGHT > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    advanceToNextPage("ধার-দেনা খাতা (চলমান)")
                }

                val rowTop = currentY
                val rowRect = RectF(MARGIN_X, rowTop, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT)

                // Soft alternating background
                if (idx % 2 == 1) {
                    bgPaint.color = Color.rgb(248, 250, 252)
                    canvas.drawRect(rowRect, bgPaint)
                }

                // Vertical column grid lines
                canvas.drawLine(lSplitSl, rowTop, lSplitSl, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(lSplitPerson, rowTop, lSplitPerson, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(lSplitType, rowTop, lSplitType, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(lSplitTotal, rowTop, lSplitTotal, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(lSplitPaid, rowTop, lSplitPaid, rowTop + ROW_HEIGHT, subtleColLinePaint)
                canvas.drawLine(lSplitDue, rowTop, lSplitDue, rowTop + ROW_HEIGHT, subtleColLinePaint)

                // Horizontal bottom row line
                canvas.drawLine(MARGIN_X, rowTop + ROW_HEIGHT, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT, gridBorderPaint)

                // Outer borders
                canvas.drawLine(MARGIN_X, rowTop, MARGIN_X, rowTop + ROW_HEIGHT, gridBorderPaint)
                canvas.drawLine(MARGIN_X + CONTENT_WIDTH, rowTop, MARGIN_X + CONTENT_WIDTH, rowTop + ROW_HEIGHT, gridBorderPaint)

                // Serial
                canvas.drawText(DateUtils.toBengaliDigits(idx + 1), MARGIN_X + 6f, rowTop + 17f, textPaint)

                // Person Name (line 1) & Phone (line 2)
                val nameDisplay = if (loan.personName.length > 18) loan.personName.take(17) + "…" else loan.personName
                canvas.drawText(nameDisplay, lSplitSl + 6f, rowTop + 13f, boldTextPaint)
                if (loan.phoneNumber.isNotBlank()) {
                    canvas.drawText(loan.phoneNumber, lSplitSl + 6f, rowTop + 23f, timePaint)
                }

                // Type Badge
                val isLent = loan.type == LOAN_TYPE_LENT
                val typeLabel = if (isLent) "ধার দিয়েছি (পাবো)" else "ধার নিয়েছি (দেবো)"
                val typePaint = if (isLent) incomeTextPaint else expenseTextPaint
                canvas.drawText(if (isLent) "পাবো" else "দেবো", lSplitPerson + 6f, rowTop + 17f, typePaint)

                // Total Amount
                canvas.drawText("৳${loan.amount}", lSplitType + 6f, rowTop + 17f, textPaint)

                // Paid Amount
                canvas.drawText("৳${loan.paidAmount}", lSplitTotal + 6f, rowTop + 17f, incomeTextPaint)

                // Remaining Due Amount
                val duePaint = if (loan.isSettled) incomeTextPaint else expenseTextPaint
                val dueText = if (loan.isSettled) "✓ পরিশোধিত" else "৳${loan.remainingAmount}"
                canvas.drawText(dueText, lSplitPaid + 6f, rowTop + 17f, duePaint)

                // Due Date
                val dueDateStr = if (loan.dueDate > 0L) {
                    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(loan.dueDate))
                } else {
                    "—"
                }
                canvas.drawText(dueDateStr, lSplitDue + 6f, rowTop + 17f, textPaint)

                currentY += ROW_HEIGHT
            }
        }

        // Draw footer on the final page
        drawPageFooter(canvas, footerPaint, currentPageNum, totalPages)
        pdfDocument.finishPage(page)

        // Save PDF to cache/reports
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val sanitizedYm = yearMonth.replace("-", "_")
        val outputFile = File(reportsDir, "Amar_Hisab_Report_$sanitizedYm.pdf")

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawPageFooter(canvas: Canvas, footerPaint: Paint, pageNumber: Int, totalPages: Int) {
        val footerY = PAGE_HEIGHT - 22f
        val footerText = "আমার হিসাব (Amar Hisab) • সম্পূর্ণ ব্যক্তিগত ও সুরক্ষিত স্থানীয় হিসাব"
        canvas.drawText(footerText, MARGIN_X, footerY, footerPaint)

        val pageNumberText = "পৃষ্ঠা ${DateUtils.toBengaliDigits(pageNumber)} / ${DateUtils.toBengaliDigits(totalPages)}"
        val pageNumWidth = footerPaint.measureText(pageNumberText)
        canvas.drawText(pageNumberText, MARGIN_X + CONTENT_WIDTH - pageNumWidth, footerY, footerPaint)
    }

    private fun simulatePageCount(expenseCount: Int, incomeCount: Int, loanCount: Int = 0): Int {
        var page = 1
        var y = MARGIN_TOP + 46f + SUMMARY_CARD_HEIGHT + 16f // Initial header + summary cards

        // Expenses simulation
        val expRows = expenseCount.coerceAtLeast(1)
        val expSpaceNeeded = SECTION_HEADER_HEIGHT + 6f + TABLE_HEADER_HEIGHT + (expRows * ROW_HEIGHT) + (if (expenseCount > 0) SUBTOTAL_ROW_HEIGHT else 0f)

        if (y + expSpaceNeeded > PAGE_HEIGHT - MARGIN_BOTTOM) {
            val availableOnFirstPage = (PAGE_HEIGHT - MARGIN_BOTTOM) - y - (SECTION_HEADER_HEIGHT + 6f + TABLE_HEADER_HEIGHT)
            val rowsFit = (availableOnFirstPage / ROW_HEIGHT).toInt().coerceAtLeast(0)
            val remRows = expRows - rowsFit

            page++
            val rowsPerPage = ((PAGE_HEIGHT - MARGIN_BOTTOM - MARGIN_TOP - 28f - TABLE_HEADER_HEIGHT) / ROW_HEIGHT).toInt().coerceAtLeast(1)
            val additionalPages = (remRows + rowsPerPage - 1) / rowsPerPage
            page += (additionalPages - 1).coerceAtLeast(0)
            val leftoverRows = remRows % rowsPerPage
            y = MARGIN_TOP + 28f + TABLE_HEADER_HEIGHT + (leftoverRows * ROW_HEIGHT) + SUBTOTAL_ROW_HEIGHT + 18f
        } else {
            y += expSpaceNeeded + 18f
        }

        // Income simulation
        val incRows = incomeCount.coerceAtLeast(1)
        val incSpaceNeeded = SECTION_HEADER_HEIGHT + 6f + TABLE_HEADER_HEIGHT + (incRows * ROW_HEIGHT) + (if (incomeCount > 0) SUBTOTAL_ROW_HEIGHT else 0f)

        if (y + incSpaceNeeded > PAGE_HEIGHT - MARGIN_BOTTOM) {
            page++
            val available = (PAGE_HEIGHT - MARGIN_BOTTOM) - (MARGIN_TOP + 28f + TABLE_HEADER_HEIGHT)
            val rowsFit = (available / ROW_HEIGHT).toInt().coerceAtLeast(0)
            val rem = incRows - rowsFit
            val rowsPerPage = ((PAGE_HEIGHT - MARGIN_BOTTOM - MARGIN_TOP - 28f - TABLE_HEADER_HEIGHT) / ROW_HEIGHT).toInt().coerceAtLeast(1)
            val add = (rem + rowsPerPage - 1) / rowsPerPage
            page += (add - 1).coerceAtLeast(0)
            val left = rem % rowsPerPage
            y = MARGIN_TOP + 28f + TABLE_HEADER_HEIGHT + (left * ROW_HEIGHT) + SUBTOTAL_ROW_HEIGHT + 18f
        } else {
            y += incSpaceNeeded + 18f
        }

        // Loans simulation
        if (loanCount > 0) {
            val loanSpaceNeeded = SECTION_HEADER_HEIGHT + 6f + TABLE_HEADER_HEIGHT + (loanCount * ROW_HEIGHT)
            if (y + loanSpaceNeeded > PAGE_HEIGHT - MARGIN_BOTTOM) {
                page++
            }
        }

        return page.coerceAtLeast(1)
    }
}
