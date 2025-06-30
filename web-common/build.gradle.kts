import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false     // Spring Boot Application의 jar파일을 비활성화
jar.enabled = true

dependencies {
    implementation(project(":common")){
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-data-jpa")
    }
    //web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //jwt token
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}