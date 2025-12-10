import org.gradle.kotlin.dsl.implementation

extra["protobufVersion"] = "4.31.0"
extra["grpcVersion"] = "1.72.0"

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.5"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allprojects {
    group = "com.unimal"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "kotlin-spring")

    tasks.register("prepareKotlinBuildScriptModel"){}		// Task 'prepareKotlinBuildScriptModel' not found in project 에러 해결 명령 전체 등록

    tasks.getByName("jar") {
        enabled = false
    }

    dependencies {
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        implementation("io.github.oshai:kotlin-logging-jvm:7.0.6")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("com.h2database:h2")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// 특정 모듈만 gRPC 라이브러리 추가
configure(listOf(
    project(":proto-common"),
    project(":user"),
    project(":photo"),
    project(":board"),
    project(":map"),
    project(":notification"),
)) {
    apply(plugin = "com.google.protobuf")

    dependencies {
        // grpc 공통 설정
        implementation("io.grpc:grpc-netty:${property("grpcVersion")}")
        implementation("io.grpc:grpc-stub:${property("grpcVersion")}")
        implementation("io.grpc:grpc-protobuf:${property("grpcVersion")}")
        implementation("io.grpc:grpc-api:${property("grpcVersion")}")
        implementation("io.grpc:grpc-core:${property("grpcVersion")}")
        implementation("com.google.protobuf:protobuf-java:${property("protobufVersion")}")
        implementation("javax.annotation:javax.annotation-api:1.3.2") // gRPC 컴파일시 javax 어노테이션 오류가 발생하지 않는다.

//        // json 타입을 DB에 저장하기 위한 라이브러리
//        implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

        // Grpc-Test-Support
        testImplementation("io.grpc:grpc-testing:${property("grpcVersion")}")
    }

    tasks.withType<Test> {
        enabled = false
    }

}

// 1. QClass 생성만 필요한 모듈 (공통)
configure(listOf(
    project(":common"),
    project(":board"), // board도 QClass 생성이 필요하므로 포함
)) {
    dependencies {
        val queryDslVersion = "5.1.0"
        add("implementation", "com.querydsl:querydsl-core:$queryDslVersion")
        add("kapt", "org.hibernate.orm:hibernate-jpamodelgen:6.6.8.Final")
        add("kapt", "com.querydsl:querydsl-apt:$queryDslVersion:jakarta")
        add("kapt", "jakarta.persistence:jakarta.persistence-api")
        add("kapt", "jakarta.annotation:jakarta.annotation-api")
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
        sourceSets.main {
            kotlin.srcDir("build/generated/source/kapt/main")
        }
    }
}

// 2. QueryDSL 런타임이 필요한 모듈 (실제 사용)
configure(listOf(
    project(":board"),
)) {
    dependencies {
        val queryDslVersion = "5.1.0"
        add("implementation", "com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    }
}
