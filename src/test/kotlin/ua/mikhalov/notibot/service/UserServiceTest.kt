package ua.mikhalov.notibot.service

import com.elbekd.bot.model.ChatId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import kotlinx.coroutines.runBlocking
import ua.mikhalov.notibot.model.User
import ua.mikhalov.notibot.model.session.Session
import ua.mikhalov.notibot.model.session.State
import ua.mikhalov.notibot.repository.UserRepository

class UserServiceTest {
    private val repository = mockk<UserRepository>()
    private val service = UserService(repository)

    @Test
    fun `updateSession updates user`() = runTest {
        val chatId = ChatId.StringId("1")
        val user = User("1", Session(State.MAIN_MENU))
        coEvery { repository.findById("1") } returns user
        coEvery { repository.save(any()) } returnsArgument 0

        val result = service.updateSession(chatId, Session(State.NOTI_INPUT))

        assertEquals(State.NOTI_INPUT, result.session.state)
        coVerify { repository.save(user) }
    }

    @Test
    fun `findById throws when missing`() = runTest {
        coEvery { repository.findById("2") } returns null
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { service.findById(ChatId.StringId("2")) }
        }
    }
}
