
extra["springCloudVersion"] = "2024.0.0"

dependencies {
	implementation("org.springframework.cloud:spring-cloud-starter-gateway")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")

	testImplementation("org.springframework.security:spring-security-test")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}
