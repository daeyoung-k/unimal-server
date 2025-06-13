package com.unimal.server.webcommon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebCommonApplication

fun main(args: Array<String>) {
    runApplication<WebCommonApplication>(*args)
}
