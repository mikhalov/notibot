package ua.mikhalov.notibot.service

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import io.mockk.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.NotiState
import java.time.LocalDateTime

class ReminderServiceTest {
    private val notiService = mockk<NotiService>()
    private val bot = mockk<Bot>()
    private val service = ReminderService(notiService, bot)

    @Test
    fun `sendReminders sends notifications`() = runTest {
        val noti = Noti(ObjectId.get(), "1", NotiState.COMPLETED, LocalDateTime.now(), "text")
        coEvery { notiService.findNotificationsToSend(any()) } returns flowOf(noti)
        val slot = slot<Flow<Noti>>()
        coEvery { bot.sendMessage(any(), any()) } returns mockk()
        coEvery { notiService.updateAll(capture(slot)) } answers { slot.captured }

        service.sendReminders()

        val updated = slot.captured.toList()
        assertEquals(1, updated.size)
        assertEquals(NotiState.SENT, updated[0].notiState)
        coVerify { bot.sendMessage(any(), any()) }
    }

    @Test
    fun `failed sends are skipped`() = runTest {
        val noti = Noti(ObjectId.get(), "1", NotiState.COMPLETED, LocalDateTime.now(), "text")
        coEvery { notiService.findNotificationsToSend(any()) } returns flowOf(noti)
        val slot = slot<Flow<Noti>>()
        coEvery { bot.sendMessage(any(), any()) } throws RuntimeException()
        coEvery { notiService.updateAll(capture(slot)) } answers { slot.captured }

        service.sendReminders()

        val updated = slot.captured.toList()
        assertEquals(0, updated.size)
    }
}
