package ua.mikhalov.notibot.service

import com.elbekd.bot.Bot
import com.elbekd.bot.model.ChatId
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.mapNotNull
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ua.mikhalov.notibot.model.NotiState
import ua.mikhalov.notibot.util.Emoticons.ALERT
import java.time.LocalDateTime

@Service
class ReminderService(
    private val notiService: NotiService,
    private val bot: Bot
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(cron = "0 * * * * *")
    suspend fun sendReminders() {
        logger.info("Started reminder sending process")
        val upperBound = LocalDateTime.now().withSecond(59)
        val sentNotifications = notiService.findNotificationsToSend(upperBound)
            .mapNotNull { noti ->
                try {
                    bot.sendMessage(ChatId.StringId(noti.chatId), "$ALERT ${noti.reminderText} $ALERT")
                    noti.copy(notiState = NotiState.SENT)
                } catch (e: Exception) {
                    logger.error("Failed to send notification: ${noti.id}", e)
                    null
                }
            }
        val count = notiService.updateAll(sentNotifications).count()
        logger.info("Sent $count notifications")
    }
}