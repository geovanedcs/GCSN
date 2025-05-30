package br.com.omnidevs.gcsn.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.periodUntil
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object DateUtils {
    fun formatToSocialDate(isoDateString: String): String {
        try {
            val instant = Instant.parse(isoDateString)
            val now = Clock.System.now()

            val difference = now - instant

            return when {
                difference < 60.seconds -> "agora"
                difference < 60.minutes -> "${(difference.inWholeMinutes)}m"
                difference < 24.hours -> "${(difference.inWholeHours)}h"
                difference < 48.hours -> "ontem"
                difference < 7.days -> "${(difference.inWholeDays)}d"
                difference < 30.days -> "${(difference.inWholeDays / 7)}sem"
                difference < 365.days -> {
                    val months = difference.inWholeDays / 30
                    if (months == 1L) "1 mês" else "$months meses"
                }
                else -> {
                    val years = difference.inWholeDays / 365
                    if (years == 1L) "1 ano" else "$years anos"
                }
            }
        } catch (e: Exception) {
            return isoDateString
        }
    }

    fun formatToFullDate(isoDateString: String): String {
        try {
            val instant = Instant.parse(isoDateString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.year
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')

            return "$day/$month/$year $hour:$minute"
        } catch (e: Exception) {
            return isoDateString
        }
    }
}