package com.unimal.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.unimal.user", "com.unimal.webcommon"])
class UserApplication

fun main(args: Array<String>) {
	runApplication<UserApplication>(*args)
}
