
val lombokVersion = "1.18.26"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.spring.io/milestone")
    }

    maven {
        url = uri("https://repo.spring.io/snapshot")
    }

    maven {
        url = uri("https://jcenter.bintray.com")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    api("io.springfox:springfox-boot-starter:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    api("org.telegram:telegrambots:6.5.0")
    api("org.telegram:telegrambots-spring-boot-starter:6.5.0")
    api("org.postgresql:postgresql:42.3.8")
    api("org.springframework:spring-jdbc:5.3.26")
    implementation(kotlin("stdlib", "1.5.21"))
    api("io.github.microutils:kotlin-logging-jvm:2.0.11")
    api("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.21")
}

plugins {
    java
    kotlin("jvm") version "1.8.21"
    `maven-publish`
    id("org.springframework.boot") version "2.7.10"
    application
}

apply(plugin = "io.spring.dependency-management")

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "demo"

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "19"
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

application {
    mainClass.set("org.example.Application")
}