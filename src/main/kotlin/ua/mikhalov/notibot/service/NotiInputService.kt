package ua.mikhalov.notibot.service

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import com.elbekd.bot.types.*
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ua.mikhalov.notibot.extentions.formatForCalendar
import ua.mikhalov.notibot.extentions.formatToUkrainian
import ua.mikhalov.notibot.extentions.getChatId
import ua.mikhalov.notibot.extentions.toLocalTime
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.NotiState
import ua.mikhalov.notibot.model.session.NotiInputSession
import ua.mikhalov.notibot.model.session.Session
import ua.mikhalov.notibot.model.session.State
import ua.mikhalov.notibot.util.Keyboards
import ua.mikhalov.notibot.util.Keyboards.NotiCreation.CANCEL_BUTTON
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class NotiInputService(
    private val bot: Bot,
    private val userService: UserService,
    private val notiService: NotiService
) {

    @Transactional
    suspend fun onCreate(chatId: ChatId) {
        val notiId = ObjectId.get()
        userService.updateSession(chatId, NotiInputSession(notiId))
        val now = LocalDate.now()
        val inlineKeyboardMarkup = Keyboards.Calendar.create(now)
        sendMessageAndRecord(chatId, "Коли нагадати?", replyMarkup = Keyboards.NotiCreation.keyboard)
        sendMessageAndRecord(
            chatId = chatId,
            text = now.formatForCalendar(),
            replyMarkup = inlineKeyboardMarkup
        )
        notiService.save(Noti(notiId, chatId.toString(), NotiState.AWAITING_DATE_SELECTION))
    }

    @Transactional
    suspend fun onMessage(session: NotiInputSession, msg: Message) {
        val chatId = msg.getChatId()
        if (msg.text == CANCEL_BUTTON) {
            clearSessionAndSetMainMenuState(chatId)
            return
        }
        recordMessage(msg)
        val noti = notiService.findNotiById(session.notiId)
        when (noti.notiState) {
            NotiState.AWAITING_TIME_INPUT -> processTimeInput(noti, msg)
            NotiState.AWAITING_REMINDER_INPUT -> processReminderInput(noti, msg)
            else -> sendMessageAndRecord(chatId, text = "Помилка виконання, почніть з початку")
        }
    }

    @Transactional
    suspend fun onCallback(session: NotiInputSession, callback: CallbackQuery) {
        val chatId = callback.getChatId()
        val noti = notiService.findNotiById(session.notiId)
        when (noti.notiState) {
            NotiState.AWAITING_DATE_SELECTION -> processDateSelectionCallback(callback, noti)
            NotiState.AWAITING_CONFIRMATION -> confirmNoti(callback, noti)
            else -> sendMessageAndRecord(chatId, text = "Помилка виконання, почніть з початку")
        }
    }

    private suspend fun confirmNoti(callback: CallbackQuery, noti: Noti) {
        val data = callback.data!!
        when {
            data.startsWith("confirm:") -> {
                notiService.updateNoti(noti, notiState = NotiState.COMPLETED)
                val chatId = callback.getChatId()
                clearSessionAndSetMainMenuState(chatId)
            }
        }
    }

    private suspend fun clearSessionAndSetMainMenuState(chatId: ChatId) {
        val session = userService.findById(chatId).session
        (session as? NotiInputSession)?.let { ids -> ids.messageIds.forEach { bot.deleteMessage(chatId, it) } }
        userService.updateSession(chatId, Session(State.MAIN_MENU))
        bot.sendMessage(chatId, "Головне меню", replyMarkup = Keyboards.Main.keyboard)
    }


    private suspend fun processDateSelectionCallback(
        callback: CallbackQuery,
        noti: Noti
    ) {
        val chatId = callback.getChatId()
        val data = callback.data!!
        when {
            data.startsWith("date:") -> {
                val selectedLocalDate =
                    LocalDate.parse(data.removePrefix("date:"), DateTimeFormatter.ISO_LOCAL_DATE)
                sendMessageAndRecord(
                    chatId,
                    "${selectedLocalDate.formatToUkrainian()}\nНа яку годину? У форматі: ГГ ХХ, наприклад, 16 20"
                )
                val messageId = callback.message!!.messageId
                deleteMessage(chatId, messageId)
                notiService.updateNoti(noti, NotiState.AWAITING_TIME_INPUT, selectedLocalDate)
            }

            data.startsWith("nextMonth:") -> {
                val date = LocalDate.parse(data.removePrefix("nextMonth:"))
                val nextMonthDate = date.withDayOfMonth(1)
                val inlineKeyboardMarkup = Keyboards.Calendar.create(nextMonthDate)
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
                val inlineKeyboardMarkup = Keyboards.Calendar.create(nextMonthDate)
                bot.editMessageText(
                    chatId,
                    callback.message?.messageId,
                    text = nextMonthDate.formatForCalendar(),
                    replyMarkup = inlineKeyboardMarkup
                )
            }
        }
    }

    private suspend fun deleteMessage(chatId: ChatId, messageId: Long) {
        bot.deleteMessage(chatId, messageId)
        val user = userService.findById(chatId)
        (user.session as NotiInputSession).messageIds.remove(messageId)
        userService.save(user)
    }


    private suspend fun processTimeInput(noti: Noti, msg: Message) {
        val chatId = msg.getChatId()
        val text = msg.text
        if (text == null) {
            sendMessageAndRecord(chatId, "Очікуються час для ноті.")
            return
        }
        val trimmedText = text.trim()
        if (!trimmedText.matches("^([01]?[0-9]|2[0-3]) [0-5][0-9]$".toRegex())) {
            sendMessageAndRecord(chatId, "Спробуйте ще раз у форматі: ГГ ММ, наприклад, 16 20")
            return
        }
        val time = trimmedText.toLocalTime()
        val currentDate = noti.notificationDateTime!!.toLocalDate()
        if (!currentDate.isAfter(LocalDate.now()) && time.isBefore(LocalTime.now())) {
            sendMessageAndRecord(chatId, "Час не може бути в минулому. Введіть будь ласка майбутній час.")
            return
        }
        notiService.updateNoti(noti, NotiState.AWAITING_REMINDER_INPUT, time = time)
        sendMessageAndRecord(chatId, "**Введіть нагадування**", parseMode = ParseMode.MarkdownV2)
    }


    private suspend fun processReminderInput(noti: Noti, msg: Message) {
        val chatId = msg.getChatId()
        val text = msg.text
        if (text.isNullOrBlank()) {
            sendMessageAndRecord(chatId, "Очікується текст для ноті")
            return
        }
        val updatedNoti = notiService.updateNoti(noti, notiState = NotiState.AWAITING_CONFIRMATION, reminderText = text)
        val messageText = """
    *Нагадування створено\!*
    `${updatedNoti.notificationDateTime!!.formatToUkrainian()}`
    `${updatedNoti.reminderText!!}`
""".trimIndent()

        val confirmationButton = InlineKeyboardButton(
            text = "Підтвердити",
            callbackData = "confirm:${updatedNoti.id}"
        )
        val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(confirmationButton)))

        sendMessageAndRecord(
            chatId = chatId,
            text = messageText,
            parseMode = ParseMode.MarkdownV2,
            replyMarkup = inlineKeyboardMarkup
        )
    }

    suspend fun sendMessageAndRecord(
        chatId: ChatId,
        text: String,
        parseMode: ParseMode? = null,
        replyMarkup: ReplyKeyboard? = null
    ): Message {
        val message = bot.sendMessage(chatId, text, parseMode = parseMode, replyMarkup = replyMarkup)
        recordMessage(message)
        return message
    }

    suspend fun recordMessage(message: Message) {
        val user = userService.findById(message.getChatId())
        (user.session as? NotiInputSession)?.messageIds?.add(message.messageId)
        userService.save(user)
    }

}
