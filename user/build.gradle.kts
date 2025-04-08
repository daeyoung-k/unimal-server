
dependencies {
	apply(plugin = "kotlin-jpa")

	//common 모듈 상속
	implementation(project(":common"))

//	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
//	implementation("org.springframework.boot:spring-boot-starter-security")
//	implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")

	//직렬화
	implementation("com.google.code.gson:gson:2.12.1")

	//web
	implementation("org.springframework.boot:spring-boot-starter-web")

	//db
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")

	//jwt token
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	testImplementation("org.springframework.security:spring-security-test")
}

