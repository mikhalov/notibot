package ua.mikhalov.notibot.model

import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NotiTest {
    @Test
    fun `setDate initializes date when null`() {
        val noti = Noti(ObjectId.get(), "1", NotiState.AWAITING_DATE_SELECTION)
        val date = LocalDate.of(2024, 5, 20)
        noti.setDate(date)
        assertEquals(LocalDateTime.of(date, LocalTime.MIN), noti.notificationDateTime)
    }

    @Test
    fun `setDate preserves existing time`() {
        val noti = Noti(ObjectId.get(), "1", NotiState.AWAITING_DATE_SELECTION,
            notificationDateTime = LocalDateTime.of(2024, 1, 1, 10, 30))
        val date = LocalDate.of(2024, 6, 15)
        noti.setDate(date)
        assertEquals(LocalDateTime.of(date, LocalTime.of(10, 30)), noti.notificationDateTime)
    }

    @Test
    fun `setTime initializes time when null`() {
        val noti = Noti(ObjectId.get(), "1", NotiState.AWAITING_TIME_INPUT)
        val time = LocalTime.of(8, 15)
        noti.setTime(time)
        assertEquals(LocalDateTime.of(LocalDate.now(), time), noti.notificationDateTime)
    }

    @Test
    fun `setTime preserves existing date`() {
        val noti = Noti(ObjectId.get(), "1", NotiState.AWAITING_TIME_INPUT,
            notificationDateTime = LocalDateTime.of(2024, 7, 30, 9, 0))
        val time = LocalTime.of(22, 5)
        noti.setTime(time)
        assertEquals(LocalDateTime.of(2024, 7, 30, 22, 5), noti.notificationDateTime)
    }
}

