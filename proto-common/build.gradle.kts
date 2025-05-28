import org.springframework.boot.gradle.tasks.bundling.BootJar
import com.google.protobuf.gradle.id

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false     // Spring Boot Application의 jar파일을 비활성화
jar.enabled = true

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