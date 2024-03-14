package ua.mikhalov.notibot.service

import com.elbekd.bot.model.ChatId
import org.springframework.stereotype.Service
import ua.mikhalov.notibot.model.User
import ua.mikhalov.notibot.model.session.Session
import ua.mikhalov.notibot.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository
) {
    suspend fun save(user: User): User = userRepository.save(user)

    suspend fun findById(chatId: ChatId): User =
        userRepository.findById(chatId.toString()) ?: throw IllegalArgumentException("User with id $chatId not found")

    suspend fun updateSession(chatId: ChatId, session: Session): User {
        val user = findById(chatId)
        user.session = session
        return userRepository.save(user)
    }
}