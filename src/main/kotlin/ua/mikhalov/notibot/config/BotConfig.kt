package ua.mikhalov.notibot.config

import com.elbekd.bot.Bot
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig {

    @Bean
    fun bot(): Bot {
        val token = "7107473763:AAFxJrBViO4x49qGDcaRvsKzcUCptMF1LW4"
        val bot = Bot.createPolling(token)
        bot.start()
        return bot
    }
}