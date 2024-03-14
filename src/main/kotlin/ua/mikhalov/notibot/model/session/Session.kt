package ua.mikhalov.notibot.model.session

open class Session(
    var state: State
)

enum class State() {
    NOTI_INPUT,
    MAIN_MENU
}
