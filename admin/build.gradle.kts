
val grpcVersion: String by rootProject.extra
val protobufVersion: String by rootProject.extra



dependencies {
    apply(plugin = "kotlin-jpa")

    //common 모듈 상속
    implementation(project(":common"))
    implementation(project(":web-common"))

    //web
    implementation("org.springframework.boot:spring-boot-starter-web")

    //actuator & prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    //db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    // JTS (Java Topology Suite) - Point, Polygon 등 핵심 객체
    implementation("org.locationtech.jts:jts-core:1.20.0")
    // JPA가 JTS 객체를 PostGIS와 매핑
    implementation("org.hibernate.orm:hibernate-spatial:6.6.8.Final")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    //kafka
    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("io.mockk:mockk:1.14.2")
}