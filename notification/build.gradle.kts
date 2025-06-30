import com.google.protobuf.gradle.id

val grpcVersion: String by rootProject.extra
val protobufVersion: String by rootProject.extra

// gRPC 설정
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}

dependencies {
    //common 모듈 상속
    // Spring Boot Starter Data JPA는 common 모듈에서 제외 상속
    implementation(project(":common")){
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-data-jpa")
    }
    implementation(project(":proto-common"))
    implementation(project(":web-common"))

    //web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //mail
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    //gRPC Server
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")

    //kafka
    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("io.mockk:mockk:1.14.2")
}