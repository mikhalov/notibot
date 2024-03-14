package ua.mikhalov.notibot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class NotibotApplication

fun main(args: Array<String>) {
    runApplication<NotibotApplication>(*args)
}