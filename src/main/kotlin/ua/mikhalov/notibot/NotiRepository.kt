package ua.mikhalov.notibot

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ua.mikhalov.notibot.model.Noti

@Repository
interface NotiRepository : CoroutineCrudRepository<Noti, String> {
}