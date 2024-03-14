package ua.mikhalov.notibot.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ua.mikhalov.notibot.model.session.Session

@Document
data class User(
    @Id val chatId: String,
    var session: Session
)
