import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false     // Spring Boot Application의 jar파일을 비활성화
jar.enabled = true

dependencies {
}