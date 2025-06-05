package ua.mikhalov.notibot.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.NotiState
import ua.mikhalov.notibot.repository.NotiRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NotiServiceTest {
    private val repository = mockk<NotiRepository>()
    private val service = NotiService(repository)

    @Test
    fun `updateNoti updates fields and saves`() = runTest {
        val noti = Noti(ObjectId.get(), "1", NotiState.AWAITING_DATE_SELECTION)
        val date = LocalDate.of(2024, 6, 15)
        val time = LocalTime.of(9, 30)
        val reminder = "text"
        coEvery { repository.save(any()) } answers { firstArg() }

        val updated = service.updateNoti(noti, NotiState.COMPLETED, date, time, reminder)

        assertEquals(NotiState.COMPLETED, updated.notiState)
        assertEquals(LocalDateTime.of(date, time), updated.notificationDateTime)
        assertEquals(reminder, updated.reminderText)
        coVerify { repository.save(noti) }
    }
}
