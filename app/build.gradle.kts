plugins {
    application
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.spring.context)
    implementation(libs.spring.webflux)
    implementation(libs.spring.tx)
    implementation(libs.spring.messaging)
    implementation(libs.spring.modulith.core)
    implementation(libs.spring.modulith.events)
    implementation(libs.reactor.netty.http)
    implementation(libs.bundles.telegram.bot)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.parameter.names)
    implementation(libs.slf4j.api)
    // Spring Data MongoDB Reactive
    implementation(libs.mongodb.driver.reactivestreams)
    implementation(libs.spring.data.mongodb)
    implementation(libs.mongodb.driver.core)
    //----------
    runtimeOnly(libs.logback.classic)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.test)
    testImplementation(libs.spring.modulith.test)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers.kafka)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "ru.spbstu.Application"
}

tasks.test {
    useJUnitPlatform()
}