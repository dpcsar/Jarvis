package site.jarviscopilot.jarvis.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

object TimeUtil {

    fun getCurrentLocalTime(): String {
        val currentMoment = Clock.System.now()
        val localDateTime = currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(
            java.time.LocalDateTime.of(
                localDateTime.year,
                localDateTime.monthNumber,
                localDateTime.dayOfMonth,
                localDateTime.hour,
                localDateTime.minute,
                localDateTime.second
            )
        )
    }

    fun getCurrentUtcTime(): String {
        val currentMoment = Clock.System.now()
        val utcDateTime = currentMoment.toLocalDateTime(TimeZone.UTC)
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(
            java.time.LocalDateTime.of(
                utcDateTime.year,
                utcDateTime.monthNumber,
                utcDateTime.dayOfMonth,
                utcDateTime.hour,
                utcDateTime.minute,
                utcDateTime.second
            )
        )
    }
}
