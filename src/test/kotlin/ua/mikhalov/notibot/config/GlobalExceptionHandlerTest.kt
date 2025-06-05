package ua.mikhalov.notibot.config

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GlobalExceptionHandlerTest {
    private val bot = mockk<Bot>(relaxed = true)
    private val handler = GlobalExceptionHandler(bot)

    @Test
    fun `handleException sends message`() = runTest {
        handler.handleException(RuntimeException("err"), ChatId.StringId("1"))
        coVerify { bot.sendMessage(any(), any()) }
    }
}
