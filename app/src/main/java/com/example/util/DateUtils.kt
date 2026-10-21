package com.example.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    val DHAKA_ZONE: ZoneId = ZoneId.of("Asia/Dhaka")

    private val BENGALI_MONTHS = arrayOf(
        "", // 1-indexed placeholder
        "জানুয়ারি",
        "ফেব্রুয়ারি",
        "মার্চ",
        "এপ্রিল",
        "মে",
        "জুন",
        "জুলাই",
        "আগস্ট",
        "সেপ্টেম্বর",
        "অক্টোবর",
        "নভেম্বর",
        "ডিসেম্বর"
    )

    private val BENGALI_DIGITS = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(BENGALI_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBengaliDigits(number: Long): String {
        return toBengaliDigits(number.toString())
    }

    fun toBengaliDigits(number: Int): String {
        return toBengaliDigits(number.toString())
    }

    fun formatTaka(amount: Long, isBn: Boolean = true, withSymbol: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        val formatted = formatter.format(amount)
        val numberStr = if (isBn) toBengaliDigits(formatted) else formatted
        return if (withSymbol) "৳ $numberStr" else numberStr
    }

    fun formatTaka(amount: Long, withSymbol: Boolean): String {
        return formatTaka(amount, isBn = true, withSymbol = withSymbol)
    }

    fun formatYearMonth(yearMonthStr: String, isBn: Boolean = true): String {
        return if (isBn) {
            formatYearMonthBengali(yearMonthStr)
        } else {
            try {
                val parts = yearMonthStr.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val ym = YearMonth.of(year, month)
                ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
            } catch (e: Exception) {
                yearMonthStr
            }
        }
    }

    fun formatDate(timestampMillis: Long, isBn: Boolean = true): String {
        return if (isBn) {
            formatDateBengali(timestampMillis)
        } else {
            val dt = Instant.ofEpochMilli(timestampMillis).atZone(DHAKA_ZONE)
            dt.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.ENGLISH))
        }
    }

    fun formatTime(timestampMillis: Long, isBn: Boolean = true): String {
        return if (isBn) {
            formatTimeBengali(timestampMillis)
        } else {
            val dt = Instant.ofEpochMilli(timestampMillis).atZone(DHAKA_ZONE)
            dt.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
        }
    }

    fun formatDigits(number: Any, isBn: Boolean = true): String {
        val s = number.toString()
        return if (isBn) toBengaliDigits(s) else s
    }

    fun currentYearMonth(): String {
        val now = ZonedDateTime.now(DHAKA_ZONE)
        return String.format(Locale.US, "%04d-%02d", now.year, now.monthValue)
    }

    fun getYearMonthFromTimestamp(timestampMillis: Long): String {
        val dt = Instant.ofEpochMilli(timestampMillis).atZone(DHAKA_ZONE)
        return String.format(Locale.US, "%04d-%02d", dt.year, dt.monthValue)
    }

    fun formatYearMonthBengali(yearMonthStr: String): String {
        return try {
            val parts = yearMonthStr.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val monthName = if (month in 1..12) BENGALI_MONTHS[month] else "মাস $month"
            "$monthName ${toBengaliDigits(year)}"
        } catch (e: Exception) {
            yearMonthStr
        }
    }

    fun formatDateBengali(timestampMillis: Long): String {
        val dt = Instant.ofEpochMilli(timestampMillis).atZone(DHAKA_ZONE)
        val day = toBengaliDigits(dt.dayOfMonth)
        val monthName = BENGALI_MONTHS[dt.monthValue]
        val year = toBengaliDigits(dt.year)
        return "$day $monthName, $year"
    }

    fun formatTimeBengali(timestampMillis: Long): String {
        val dt = Instant.ofEpochMilli(timestampMillis).atZone(DHAKA_ZONE)
        val hour24 = dt.hour
        val minute = dt.minute

        val period = when (hour24) {
            in 4..5 -> "ভোর"
            in 6..11 -> "সকাল"
            in 12..14 -> "দুপুর"
            in 15..17 -> "বিকাল"
            in 18..19 -> "সন্ধ্যা"
            else -> "রাত"
        }

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        val timeStr = String.format(Locale.US, "%02d:%02d", hour12, minute)
        return "$period ${toBengaliDigits(timeStr)}"
    }

    fun formatDateTimeBengali(timestampMillis: Long): String {
        return "${formatDateBengali(timestampMillis)} • ${formatTimeBengali(timestampMillis)}"
    }

    fun previousMonth(yearMonthStr: String): String {
        val ym = YearMonth.parse(yearMonthStr)
        val prev = ym.minusMonths(1)
        return String.format(Locale.US, "%04d-%02d", prev.year, prev.monthValue)
    }

    fun nextMonth(yearMonthStr: String): String {
        val ym = YearMonth.parse(yearMonthStr)
        val next = ym.plusMonths(1)
        return String.format(Locale.US, "%04d-%02d", next.year, next.monthValue)
    }

    fun parseAmount(text: String): Long? {
        val trimmed = text.trim()
            .replace("৳", "")
            .replace(",", "")
            .trim()
        // Convert any Bengali digits to standard Latin digits if entered
        val normalized = trimmed.map { ch ->
            val idx = BENGALI_DIGITS.indexOf(ch)
            if (idx >= 0) ('0' + idx) else ch
        }.joinToString("")

        return normalized.toLongOrNull()
    }
}
