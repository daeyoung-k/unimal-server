import com.google.protobuf.gradle.id

plugins {
    id("com.google.protobuf") version "0.9.4"
}

// gRPC 설정
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.31.0"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.72.0"
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