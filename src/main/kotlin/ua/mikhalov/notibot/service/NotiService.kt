package ua.mikhalov.notibot.service

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.NotiState
import ua.mikhalov.notibot.repository.NotiRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class NotiService(
    private val notiRepository: NotiRepository
) {
    suspend fun findNotiById(id: ObjectId): Noti {
        return notiRepository.findById(id) ?: throw IllegalStateException("Noti with id $id not found")
    }

    suspend fun updateNoti(
        noti: Noti,
        notiState: NotiState? = null,
        date: LocalDate? = null,
        time: LocalTime? = null,
        reminderText: String? = null
    ): Noti {
        if (notiState != null) noti.notiState = notiState
        if (date != null) noti.setDate(date)
        if (time != null) noti.setTime(time)
        if (reminderText != null) noti.reminderText = reminderText
        return save(noti)
    }

    suspend fun save(noti: Noti): Noti = notiRepository.save(noti)


    fun findNotificationsToSend(dateTime: LocalDateTime): Flow<Noti> {
        return notiRepository.findByNotificationDateTimeBeforeAndNotiStateIs(dateTime, NotiState.COMPLETED)
    }

    fun updateAll(sentNotifications: Flow<Noti>): Flow<Noti> = notiRepository.saveAll(sentNotifications)
}