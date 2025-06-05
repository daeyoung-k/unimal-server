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
    apply(plugin = "kotlin-jpa")

    //common 모듈 상속
    implementation(project(":common"))
    implementation(project(":proto-common"))

    //직렬화
    implementation("com.google.code.gson:gson:2.12.1")

    //web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    //gRPC Server
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")

    testImplementation("io.mockk:mockk:1.14.2")
}
