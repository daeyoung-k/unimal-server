package com.unimal.map

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.unimal.map", "com.unimal.webcommon"])
class MapApplication

fun main(args: Array<String>) {
    runApplication<MapApplication>(*args)
}
