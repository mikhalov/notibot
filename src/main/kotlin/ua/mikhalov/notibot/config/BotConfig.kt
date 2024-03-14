package ua.mikhalov.notibot.config

import com.elbekd.bot.Bot
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig {

    @Bean
    fun bot(@Value("\${telegram.api.key}") key: String): Bot {
        val bot = Bot.createPolling(key)
        bot.start()
        return bot
    }
}