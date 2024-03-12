package ua.mikhalov.notibot.service

import com.elbekd.bot.model.ChatId
import org.springframework.stereotype.Service
import ua.mikhalov.notibot.NotiRepository
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.State
import java.time.LocalDate
import java.time.LocalTime

@Service
class NotiService(
    private val notiRepository: NotiRepository
) {
    suspend fun findNotiByIdOrThrow(id: ChatId): Noti {
        return notiRepository.findById(id.toString()) ?: throw IllegalStateException("Noti with id $id not found")
    }

    suspend fun updateNoti(
        id: ChatId,
        state: State? = null,
        date: LocalDate? = null,
        time: LocalTime? = null,
        reminderText: String? = null
    ): Noti {
        val noti = findNotiByIdOrThrow(id)
        return updateNoti(noti, state, date, time, reminderText)
    }

    suspend fun updateNoti(
        noti: Noti,
        state: State? = null,
        date: LocalDate? = null,
        time: LocalTime? = null,
        reminderText: String? = null
    ): Noti {
        if (state != null) noti.state = state
        if (date != null) noti.setDate(date)
        if (time != null) noti.setTime(time)
        if (reminderText != null) noti.reminderText = reminderText
        return save(noti)
    }

    suspend fun save(noti: Noti): Noti = notiRepository.save(noti)

    suspend fun isStateEqual(chatId: ChatId, state: State): Boolean = findNotiByIdOrThrow(chatId).state == state

    suspend fun getState(chatId: ChatId): State = findNotiByIdOrThrow(chatId).state
}