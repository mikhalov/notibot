package ua.mikhalov.notibot.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ua.mikhalov.notibot.model.User

@Repository
interface UserRepository : CoroutineCrudRepository<User, String> {
}