package ua.mikhalov.notibot.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class KeyboardsTest {
    @Test
    fun `calendar for current month contains only next button`() {
        val date = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth() - 2)
        val markup = Keyboards.Calendar.create(date)
        val lastRow = markup.inlineKeyboard.last()
        assertEquals(1, lastRow.size)
    }

    @Test
    fun `calendar for non current month has prev and next buttons`() {
        val date = LocalDate.now().minusMonths(1).withDayOfMonth(15)
        val markup = Keyboards.Calendar.create(date)
        val lastRow = markup.inlineKeyboard.last()
        assertEquals(2, lastRow.size)
    }
    @Test
    fun `calendar rows contain up to seven days`() {
        val date = LocalDate.of(2024, 1, 1)
        val markup = Keyboards.Calendar.create(date)
        markup.inlineKeyboard.dropLast(1).forEach { row ->
            assert(row.size <= 7)
        }
    }
}
