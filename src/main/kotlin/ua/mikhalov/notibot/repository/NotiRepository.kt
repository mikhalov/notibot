package ua.mikhalov.notibot.repository

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ua.mikhalov.notibot.model.Noti
import ua.mikhalov.notibot.model.NotiState
import java.time.LocalDateTime

@Repository
interface NotiRepository : CoroutineCrudRepository<Noti, ObjectId> {
    fun findByNotificationDateTimeBeforeAndNotiStateIs(dateTime: LocalDateTime, notiState: NotiState): Flow<Noti>
}