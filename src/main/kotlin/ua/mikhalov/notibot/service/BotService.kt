package ua.mikhalov.notibot.service

import com.elbekd.bot.Bot
import com.elbekd.bot.types.*
import org.springframework.stereotype.Service
import ua.mikhalov.notibot.config.GlobalExceptionHandler
import ua.mikhalov.notibot.extentions.getChatId
import ua.mikhalov.notibot.model.User
import ua.mikhalov.notibot.model.session.NotiInputSession
import ua.mikhalov.notibot.model.session.Session
import ua.mikhalov.notibot.model.session.State
import ua.mikhalov.notibot.util.Keyboards
import ua.mikhalov.notibot.util.Keyboards.Main.NOTI_BUTTON

@Service
class BotService(
    private val notiInputService: NotiInputService,
    private val userService: UserService,
    private val bot: Bot,
    private val globalExceptionHandler: GlobalExceptionHandler
) {
    init {
        bot.onCommand("/start", onStart())
        bot.onCallbackQuery(onCallback())
        bot.onMessage(onMessage())
        bot.onAnyUpdate {
        }
    }

    private fun onMessage(): suspend (Message) -> Unit = { msg ->
        val chatId = msg.getChatId()
        try {
            val user = userService.findById(chatId)
            val session = user.session
            when (session.state) {
                State.NOTI_INPUT -> (session as? NotiInputSession)?.let { notiInputService.onMessage(it, msg) }
                State.MAIN_MENU -> if (msg.text == NOTI_BUTTON) notiInputService.onCreate(chatId) else bot.sendMessage(
                    chatId,
                    "Оберіть дію"
                )
            }
        } catch (e: Exception) {
            globalExceptionHandler.handleException(e, chatId)
        }
    }

    private fun onCallback(): suspend (CallbackQuery) -> Unit = { callback ->
        val chatId = callback.getChatId()
        try {
            val user = userService.findById(chatId)
            val session = user.session
            when (session.state) {
                State.NOTI_INPUT -> (session as? NotiInputSession)?.let { notiInputService.onCallback(it, callback) }
                State.MAIN_MENU -> bot.sendMessage(chatId, "Оберіть дію")
            }
        } catch (e: Exception) {
            globalExceptionHandler.handleException(e, chatId)
        }
    }

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
                text = "Привіт, ти можеш створити ноті, та я тобі нагадаю",
                parseMode = ParseMode.MarkdownV2,
                replyMarkup = Keyboards.Main.keyboard
            )
            userService.save(User(chatId.toString(), Session(State.MAIN_MENU)))
        }
}