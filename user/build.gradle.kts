
dependencies {
	apply(plugin = "kotlin-jpa")
	implementation(project(":common"))      // common 모듈 상속

	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
	implementation("com.google.code.gson:gson:2.12.1")

	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.security:spring-security-test")
}

