package ua.mikhalov.notibot.model.session

import org.bson.types.ObjectId

data class NotiInputSession(
    val notiId: ObjectId,
    val messageIds: MutableList<Long> = mutableListOf()
) : Session(state = State.NOTI_INPUT)