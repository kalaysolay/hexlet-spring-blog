plugins {
        java
        id("org.springframework.boot") version "3.5.5"
        id("io.spring.dependency-management") version "1.1.7"
}

group = "io.hexlet"
version = "0.0.1-SNAPSHOT"
description = "Task Manager"

java {
        toolchain {
                languageVersion = JavaLanguageVersion.of(17)
        }
}

repositories {
        mavenCentral()
}

dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-devtools")
        implementation("org.springframework.security:spring-security-crypto")
        implementation("com.auth0:java-jwt:4.4.0")
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
        implementation("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
        runtimeOnly("com.h2database:h2")
        runtimeOnly("org.postgresql:postgresql")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<Test> {
        useJUnitPlatform()
}
