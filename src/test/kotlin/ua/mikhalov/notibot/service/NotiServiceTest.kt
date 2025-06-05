package ua.mikhalov.notibot.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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

    @Test
    fun `findNotiById returns noti`() = runTest {
        val noti = Noti(ObjectId.get(), "1", NotiState.AWAITING_DATE_SELECTION)
        coEvery { repository.findById(noti.id) } returns noti

        val result = service.findNotiById(noti.id)

        assertEquals(noti, result)
    }

    @Test
    fun `findNotiById throws`() = runTest {
        val id = ObjectId.get()
        coEvery { repository.findById(id) } returns null

        assertThrows(IllegalStateException::class.java) { runBlocking { service.findNotiById(id) } }
    }

    @Test
    fun `updateAll saves flow`() = runTest {
        val noti = Noti(ObjectId.get(), "1", NotiState.SENT)
        val flow = flowOf(noti)
        coEvery { repository.saveAll(any<Flow<Noti>>()) } answers { firstArg() }

        val result = service.updateAll(flow).toList()

        assertEquals(1, result.size)
        coVerify { repository.saveAll(any<Flow<Noti>>()) }
    }
}
