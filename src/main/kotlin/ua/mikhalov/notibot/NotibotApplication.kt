package ua.mikhalov.notibot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotibotApplication

fun main(args: Array<String>) {
    runApplication<NotibotApplication>(*args)
}