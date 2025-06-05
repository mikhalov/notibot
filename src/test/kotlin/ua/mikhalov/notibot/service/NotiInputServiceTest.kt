package ua.mikhalov.notibot.service

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import com.elbekd.bot.types.Message
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible
import ua.mikhalov.notibot.extentions.getChatId
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.NotiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotiInputServiceTest {
    private val bot = mockk<Bot>()
    private val userService = mockk<UserService>()
    private val notiService = mockk<NotiService>()
    private lateinit var service: NotiInputService

    @BeforeEach
    fun init() {
        service = spyk(NotiInputService(bot, userService, notiService))
        coEvery { service.recordMessage(any()) } just Runs
        coEvery { service.sendMessageAndRecord(any(), any(), any(), any()) } returns mockk(relaxed = true)
    }

    @Test
    fun `past time is rejected`() = runTest {
        val noti = Noti(ObjectId(), "1", NotiState.AWAITING_TIME_INPUT).apply {
            setDate(LocalDate.now())
        }
        val timePast = LocalTime.now().minusMinutes(1).withSecond(0).withNano(0)
        val message = mockk<Message>()
        every { message.text } returns timePast.format(DateTimeFormatter.ofPattern("H mm"))

        mockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
        val chatId = ChatId.IntegerId(123L)
        every { message.getChatId() } returns chatId

        coEvery { notiService.updateNoti(any(), any(), any(), any(), any()) } returns noti

        val method = NotiInputService::class.declaredFunctions.first { it.name == "processTimeInput" }
        method.isAccessible = true
        method.callSuspend(service, noti, message)

        coVerify { service.sendMessageAndRecord(any(), "Час не може бути в минулому. Введіть будь ласка майбутній час.", parseMode = null, replyMarkup = null) }
        coVerify(exactly = 0) { notiService.updateNoti(any(), any(), any(), any(), any()) }
        unmockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
    }

    @Test
    fun `future time accepted`() = runTest {
        val noti = Noti(ObjectId(), "1", NotiState.AWAITING_TIME_INPUT).apply {
            setDate(LocalDate.now())
        }
        val timeFuture = LocalTime.now().plusMinutes(10).withSecond(0).withNano(0)
        val expectedTime = LocalTime.of(timeFuture.hour, timeFuture.minute)
        val message = mockk<Message>()
        every { message.text } returns timeFuture.format(DateTimeFormatter.ofPattern("H mm"))

        mockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
        val chatId = ChatId.IntegerId(123L)
        every { message.getChatId() } returns chatId

        coEvery { notiService.updateNoti(any(), NotiState.AWAITING_REMINDER_INPUT, date = null, time = expectedTime, reminderText = null) } returns noti

        val method = NotiInputService::class.declaredFunctions.first { it.name == "processTimeInput" }
        method.isAccessible = true
        method.callSuspend(service, noti, message)

        coVerify { notiService.updateNoti(any(), NotiState.AWAITING_REMINDER_INPUT, date = null, time = expectedTime, reminderText = null) }
        coVerify { service.sendMessageAndRecord(any(), "**Введіть нагадування**", parseMode = any(), replyMarkup = null) }
        unmockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
    }
}
