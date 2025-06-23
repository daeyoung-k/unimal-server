package com.unimal.board

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.unimal.board", "com.unimal.webcommon"])
class BoardApplication

fun main(args: Array<String>) {
    runApplication<BoardApplication>(*args)
}
