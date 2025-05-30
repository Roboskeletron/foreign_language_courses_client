plugins {
    id("java")
    id("application")
}

group = "ru.vsu.foreign_language_courses_client"
version = "1.0-SNAPSHOT"

application {
    mainClass = "ru.vsu.foreign_language_courses_client.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-web:6.1.12")
    implementation ("com.github.javafaker:javafaker:1.0.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}