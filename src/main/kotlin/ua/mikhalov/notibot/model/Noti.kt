package ua.mikhalov.notibot.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Document
data class Noti(
    @Id val chatId: String,
    var state: State,
    var notificationDateTime: LocalDateTime? = null,
    var reminderText: String? = null
) {
    fun setDate(date: LocalDate) {
        notificationDateTime = if (notificationDateTime == null) {
            LocalDateTime.of(date, LocalTime.MIN)
        } else {
            LocalDateTime.of(date, notificationDateTime!!.toLocalTime())
        }
    }

    fun setTime(time: LocalTime) {
        notificationDateTime = if (notificationDateTime == null) {
            LocalDateTime.of(LocalDate.now(), time)
        } else {
            LocalDateTime.of(notificationDateTime!!.toLocalDate(), time)
        }
    }
}

enum class State {
    AWAITING_NOTI_SELECTION,
    AWAITING_DATE_SELECTION,
    AWAITING_TIME_INPUT,
    AWAITING_REMINDER_INPUT,
    AWAITING_CONFIRMATION,
    COMPLETED
}
