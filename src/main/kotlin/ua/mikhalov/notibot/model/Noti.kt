package ua.mikhalov.notibot.model

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.FieldType
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Document
data class Noti(
    @MongoId(value = FieldType.OBJECT_ID) val id: ObjectId,
    val chatId: String,
    var notiState: NotiState,
    var notificationDateTime: LocalDateTime? = null,
    var reminderText: String? = null
) {
    fun setDate(date: LocalDate) {
        notificationDateTime = if (notificationDateTime == null) {
            LocalDateTime.of(date, LocalTime.MIN)
        } else {
            LocalDateTime.of(date, notificationDateTime!!.toLocalTime()).withSecond(0)
        }
    }

    fun setTime(time: LocalTime) {
        notificationDateTime = if (notificationDateTime == null) {
            LocalDateTime.of(LocalDate.now(), time)
        } else {
            LocalDateTime.of(notificationDateTime!!.toLocalDate(), time).withSecond(0)
        }
    }
}

enum class NotiState {
    AWAITING_DATE_SELECTION,
    AWAITING_TIME_INPUT,
    AWAITING_REMINDER_INPUT,
    AWAITING_CONFIRMATION,
    COMPLETED,
    SENT
}
