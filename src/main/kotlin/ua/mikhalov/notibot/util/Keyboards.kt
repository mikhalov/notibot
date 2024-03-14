package ua.mikhalov.notibot.util

import com.elbekd.bot.types.InlineKeyboardButton
import com.elbekd.bot.types.InlineKeyboardMarkup
import com.elbekd.bot.types.KeyboardButton
import com.elbekd.bot.types.ReplyKeyboardMarkup
import java.time.LocalDate


object Keyboards {

    object Main {
        const val NOTI_BUTTON = "Додати ноті"
        val keyboard: ReplyKeyboardMarkup = ReplyKeyboardMarkup(
            keyboard = listOf(
                listOf(KeyboardButton(text = NOTI_BUTTON))
            ),
            resizeKeyboard = true,
            oneTimeKeyboard = true
        )
    }

    object NotiCreation {
        const val CANCEL_BUTTON = "Скасувати ноті"
        val keyboard: ReplyKeyboardMarkup = ReplyKeyboardMarkup(
            keyboard = listOf(
                listOf(KeyboardButton(text = CANCEL_BUTTON))
            ),
            resizeKeyboard = true,
            oneTimeKeyboard = false
        )
    }

    object Calendar {
        fun create(fromDate: LocalDate): InlineKeyboardMarkup {
            val leftDays = fromDate.lengthOfMonth() - fromDate.dayOfMonth
            val buttons = (0..leftDays).map { offset ->
                val date = fromDate.plusDays(offset.toLong())
                InlineKeyboardButton(text = "  ${date.dayOfMonth}  ", callbackData = "date:${date}")
            }
            val previousMonthButton = InlineKeyboardButton(
                text = "Попередній місяць",
                callbackData = "prevMonth:${fromDate.minusMonths(1)}"
            )
            val nextMonthButton = InlineKeyboardButton(
                text = "Наступний місяць",
                callbackData = "nextMonth:${fromDate.plusMonths(1)}"
            )
            val monthButtons = if (fromDate.month != LocalDate.now().month || fromDate.year != LocalDate.now().year) {
                listOf(previousMonthButton, nextMonthButton)
            } else listOf(nextMonthButton)

            return InlineKeyboardMarkup(buttons.chunked(7).flatMap { listOf(it) } + listOf(monthButtons))
        }
    }
}