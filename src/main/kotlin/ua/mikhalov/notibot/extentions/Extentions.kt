package ua.mikhalov.notibot.extentions

import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.CallbackQuery
import com.elbekd.bot.types.Message
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

private val MONTH_NAMES = listOf(
    "Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень",
    "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень"
)

fun LocalDate.formatToUkrainian() =
    format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("uk-UA")))

fun LocalDateTime.formatToUkrainian() =
    format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.forLanguageTag("uk-UA")))

fun String.toLocalTime(): LocalTime = LocalTime.parse(this, DateTimeFormatter.ofPattern("H m"))

fun LocalDate.formatForCalendar(): String {
    val monthName = MONTH_NAMES[monthValue - 1]
    val year = year
    return "$monthName $year"
}

fun Message.getChatId() = chat.id.toChatId()

fun CallbackQuery.getChatId() = message!!.getChatId()
