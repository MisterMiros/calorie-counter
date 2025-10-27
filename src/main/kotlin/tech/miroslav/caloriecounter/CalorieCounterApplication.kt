package tech.miroslav.caloriecounter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CalorieCounterApplication

fun main(args: Array<String>) {
	runApplication<CalorieCounterApplication>(*args)
}
