dependencies {
    apply(plugin = "kotlin-jpa")

    //common 모듈 상속
    implementation(project(":common"))

    //직렬화
    implementation("com.google.code.gson:gson:2.12.1")

    //web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    testImplementation("io.mockk:mockk:1.14.2")
}