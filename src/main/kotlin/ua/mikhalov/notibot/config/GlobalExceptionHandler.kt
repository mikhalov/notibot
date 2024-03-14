package ua.mikhalov.notibot.config

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler(
    private val bot: Bot
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    suspend fun handleException(e: Throwable, chatId: ChatId) {
        logger.error("Виникла помилка: ${e.message}")
        bot.sendMessage(chatId, "Сталася помилка, будь ласка, спробуйте cпочатку. /start")
    }
}