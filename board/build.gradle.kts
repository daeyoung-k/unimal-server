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
    implementation(project(":web-common"))

    //직렬화
    implementation("com.google.code.gson:gson:2.12.1")

    //web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    // JTS (Java Topology Suite) - Point, Polygon 등 핵심 객체
    implementation("org.locationtech.jts:jts-core:1.20.0")
    // JPA가 JTS 객체를 PostGIS와 매핑
    implementation("org.hibernate.orm:hibernate-spatial:6.6.8.Final")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    //gRPC Client
    implementation("net.devh:grpc-client-spring-boot-starter:3.1.0.RELEASE")

    //kafka
    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("io.mockk:mockk:1.14.2")
}