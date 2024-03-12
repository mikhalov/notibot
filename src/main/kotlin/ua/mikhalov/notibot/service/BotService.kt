package ua.mikhalov.notibot.service

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import com.elbekd.bot.model.toChatId
import com.elbekd.bot.types.*
import org.springframework.stereotype.Service
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.State
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class BotService(
    private val bot: Bot,
    private val notiService: NotiService
) {
    companion object {
        private val MONTH_NAMES = listOf(
            "Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень",
            "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень"
        )

        private fun LocalDate.formatToUkrainian() =
            format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("uk-UA")))

        private fun LocalDateTime.formatToUkrainian() =
            format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.forLanguageTag("uk-UA")))

        private fun String.toLocalTime(): LocalTime = LocalTime.parse(this, DateTimeFormatter.ofPattern("H m"))

        private fun LocalDate.formatForCalendar(): String {
            val monthName = MONTH_NAMES[monthValue - 1]
            val year = year
            return "$monthName $year"
        }
    }

    init {
        bot.onCommand("/start", onStart())
        bot.onCallbackQuery("noti", onNoti())
        bot.onCallbackQuery(onCallback())
        bot.onMessage(onMessage())
        bot.start()
    }

    private fun onMessage(): suspend (Message) -> Unit = { msg ->
        val chatId = msg.getChatId()
        val state = notiService.getState(chatId)
        when (state) {
            State.AWAITING_TIME_INPUT -> processTimeInput(chatId, msg.text)
            State.AWAITING_REMINDER_INPUT -> processReminderInput(msg)
            else -> bot.sendMessage(chatId, text = "Помилка виконання, почніть з початку")
        }
    }

    private suspend fun processReminderInput(msg: Message) {
        val chatId = msg.getChatId()
        val text = msg.text
        if (text.isNullOrBlank()) {
            bot.sendMessage(chatId, "Очікується текст для ноті")
            return
        }
        val noti = notiService.updateNoti(chatId, state = State.AWAITING_CONFIRMATION, reminderText = text)
        val messageText = """
    *Нагадування створено\!*
    `${noti.notificationDateTime!!.formatToUkrainian()}`
    `${noti.reminderText!!.replace("-", "\\-").replace("_", "\\_")}`
""".trimIndent()

        val confirmationButton = InlineKeyboardButton(
            text = "Підтвердити",
            callbackData = "confirm:${noti.chatId}"
        )
        val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(confirmationButton)))

        bot.sendMessage(
            chatId = chatId,
            text = messageText,
            parseMode = ParseMode.MarkdownV2,
            replyMarkup = inlineKeyboardMarkup
        )
    }

    private suspend fun processTimeInput(chatId: ChatId, text: String?) {
        if (text == null) {
            bot.sendMessage(chatId, "Очікуються час для ноті.")
            return
        }
        val trimmedText = text.trim()
        if (!trimmedText.matches("^([01]?[0-9]|2[0-3]) [0-5][0-9]$".toRegex())) {
            bot.sendMessage(chatId, "Спробуйте ще раз у форматі: ГГ ММ, наприклад, 16 20")
            return
        }
        val time = trimmedText.toLocalTime()
        var noti = notiService.findNotiByIdOrThrow(chatId)
        val currentDate = noti.notificationDateTime!!.toLocalDate()
        if (!currentDate.isAfter(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            bot.sendMessage(chatId, "Час не може бути в минулому. Введіть будь ласка майбутній час.")
            return
        }
        noti = notiService.updateNoti(chatId, State.AWAITING_REMINDER_INPUT, time = time)
        val dateTimeText = noti.notificationDateTime!!.formatToUkrainian()
        bot.sendMessage(chatId, /*Час встановлено: $dateTimeText\n*/"Введіть нагадування", parseMode = ParseMode.Html)
    }

    private fun onCallback(): suspend (CallbackQuery) -> Unit = { callback ->
        val chatId = callback.getChatId()
        val state = notiService.getState(chatId)
        when (state) {
            State.AWAITING_DATE_SELECTION -> processDateSelectionCallback(callback)
            State.AWAITING_CONFIRMATION -> confirmNoti(callback)
            else -> bot.sendMessage(chatId, text = "Помилка виконання, почніть з початку")
        }
    }

    private suspend fun confirmNoti(callback: CallbackQuery) {
        val data = callback.data!!
        when {
            data.startsWith("confirm:") -> {
                notiService.updateNoti(callback.getChatId(), state = State.COMPLETED)
                bot.deleteMessage(callback.getChatId(), callback.message!!.messageId)
            }
        }
    }

    private suspend fun processDateSelectionCallback(
        callback: CallbackQuery
    ) {
        val chatId = callback.getChatId()
        val data = callback.data!!
        when {
            data.startsWith("date:") -> {
                val selectedLocalDate =
                    LocalDate.parse(data.removePrefix("date:"), DateTimeFormatter.ISO_LOCAL_DATE)
                bot.sendMessage(
                    chatId,
                    "${selectedLocalDate.formatToUkrainian()}\nНа яку годину? У форматі: ГГ ММ, наприклад, 16 20"
                )
                bot.deleteMessage(chatId, callback.message!!.messageId)
                notiService.updateNoti(chatId, State.AWAITING_TIME_INPUT, selectedLocalDate)
            }

            data.startsWith("nextMonth:") -> {
                val date = LocalDate.parse(data.removePrefix("nextMonth:"))
                val nextMonthDate = date.withDayOfMonth(1)
                val inlineKeyboardMarkup = createCalendar(nextMonthDate)
                bot.editMessageText(
                    chatId,
                    callback.message?.messageId,
                    text = nextMonthDate.formatForCalendar(),
                    replyMarkup = inlineKeyboardMarkup
                )
            }

            data.startsWith("prevMonth:") -> {
                val date = LocalDate.parse(data.removePrefix("prevMonth:"))
                val nextMonthDate = date.withDayOfMonth(1)
                val inlineKeyboardMarkup = createCalendar(nextMonthDate)
                bot.editMessageText(
                    chatId,
                    callback.message?.messageId,
                    text = nextMonthDate.formatForCalendar(),
                    replyMarkup = inlineKeyboardMarkup
                )
            }
        }
    }

    private fun onNoti(): suspend (CallbackQuery) -> Unit = { callback ->
        val id = callback.getChatId()
        bot.sendMessage(id, "Коли нагадати?")
        val now = LocalDate.now()
        val inlineKeyboardMarkup = createCalendar(now)
        bot.sendMessage(chatId = id, text = now.formatForCalendar(), replyMarkup = inlineKeyboardMarkup)
        notiService.updateNoti(id, State.AWAITING_DATE_SELECTION)
        println(notiService.updateNoti(id, State.AWAITING_DATE_SELECTION).state)
    }

    private fun createCalendar(fromDate: LocalDate): InlineKeyboardMarkup {
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

    private fun Message.getChatId() = chat.id.toChatId()

    private fun CallbackQuery.getChatId() = message!!.getChatId()

    private fun getCommands() = listOf(
        BotCommand("/start", "start"),
    )

    private fun onStart(): suspend (Pair<Message, String?>) -> Unit =
        { (msg, _) ->
            bot.setMyCommands(
                commands = getCommands(),
                scope = BotCommandScope.BotCommandScopeAllGroupChats
            )
            val chatId = msg.getChatId()
            bot.sendMessage(
                chatId = chatId,
                text = "Привіт",
                parseMode = ParseMode.MarkdownV2,
                replyMarkup = InlineKeyboardMarkup(
                    listOf(
                        listOf(
                            InlineKeyboardButton(
                                text = "Встановити ноті",
                                callbackData = "noti"
                            )
                        )
                    )
                )
            )
            notiService.save(Noti(chatId.toString(), State.AWAITING_NOTI_SELECTION))
        }
}