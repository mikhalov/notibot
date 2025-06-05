package ua.mikhalov.notibot.extentions

import com.elbekd.bot.types.CallbackQuery
import com.elbekd.bot.types.Message
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class ExtentionsTest {
    @Test
    fun `formatToUkrainian for LocalDate`() {
        val date = LocalDate.of(2024, 1, 5)
        val formatted = date.formatToUkrainian()
        // Expect formatted string to contain day and year in Ukrainian locale
        assertTrue(formatted.contains("05"))
        assertTrue(formatted.contains("2024"))
    }

    @Test
    fun `toLocalTime parses`() {
        val time = "8 05".toLocalTime()
        assertEquals(LocalTime.of(8,5), time)
    }

    @Test
    fun `getChatId from Message`() {
        val msg = mockk<Message>()
        every { msg.chat.id } returns 123L
        val id = msg.getChatId()
        assertEquals("123", id.toString())
    }

    @Test
    fun `getChatId from CallbackQuery`() {
        val msg = mockk<Message>()
        every { msg.chat.id } returns 123L
        val callback = CallbackQuery(id = "1", from = mockk(), chatInstance = "", data = null, message = msg)
        val id = callback.getChatId()
        assertEquals("123", id.toString())
    }
}
