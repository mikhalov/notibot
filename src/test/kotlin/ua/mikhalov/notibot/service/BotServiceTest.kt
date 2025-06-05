package ua.mikhalov.notibot.service

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import com.elbekd.bot.types.BotCommand
import com.elbekd.bot.types.BotCommandScope
import com.elbekd.bot.types.Message
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ua.mikhalov.notibot.config.GlobalExceptionHandler
import ua.mikhalov.notibot.extentions.getChatId
import ua.mikhalov.notibot.model.User
import ua.mikhalov.notibot.model.session.Session
import ua.mikhalov.notibot.model.session.State

class BotServiceTest {
    private val notiInputService = mockk<NotiInputService>()
    private val userService = mockk<UserService>()
    private val bot = mockk<Bot>(relaxed = true)
    private val exceptionHandler = mockk<GlobalExceptionHandler>(relaxed = true)
    private lateinit var service: BotService

    @BeforeEach
    fun init() {
        service = BotService(notiInputService, userService, bot, exceptionHandler)
    }

    @Test
    fun `onStart saves user`() = runTest {
        mockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
        val msg = mockk<Message>()
        every { msg.chat.id } returns 1L
        every { msg.getChatId() } returns ChatId.IntegerId(1)
        val method = BotService::class.declaredFunctions.first { it.name == "onStart" }
        method.isAccessible = true
        val lambda = method.call(service) as suspend (Pair<Message, String?>) -> Unit

        coEvery { userService.save(any()) } returns User("1", Session(State.MAIN_MENU))

        lambda(Pair(msg, null))

        coVerify { bot.sendMessage(any(), any(), parseMode = any(), replyMarkup = any()) }
        coVerify { userService.save(any()) }
        unmockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
    }

    @Test
    fun `onMessage routes to create`() = runTest {
        mockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
        val msg = mockk<Message>()
        every { msg.getChatId() } returns ChatId.IntegerId(1)
        every { msg.text } returns "Додати ноті"
        val user = User("1", Session(State.MAIN_MENU))
        coEvery { userService.findById(any()) } returns user
        coJustRun { notiInputService.onCreate(any()) }

        val method = BotService::class.declaredFunctions.first { it.name == "onMessage" }
        method.isAccessible = true
        val lambda = method.call(service) as suspend (Message) -> Unit

        lambda(msg)

        coVerify { notiInputService.onCreate(any()) }
        unmockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
    }

    @Test
    fun `onMessage handles exception`() = runTest {
        mockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
        val msg = mockk<Message>()
        every { msg.getChatId() } returns ChatId.IntegerId(1)
        coEvery { userService.findById(any()) } throws RuntimeException()
        val method = BotService::class.declaredFunctions.first { it.name == "onMessage" }
        method.isAccessible = true
        val lambda = method.call(service) as suspend (Message) -> Unit

        lambda(msg)

        coVerify { exceptionHandler.handleException(any(), any()) }
        unmockkStatic("ua.mikhalov.notibot.extentions.ExtentionsKt")
    }
}
