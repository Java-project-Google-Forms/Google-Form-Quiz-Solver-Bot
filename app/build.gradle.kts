plugins {
    application
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Spring Core + WebFlux
    implementation(libs.spring.context)
    implementation(libs.spring.webflux)
    implementation(libs.spring.tx)
    implementation(libs.spring.messaging)

    // Spring Modulith (module organization + event infrastructure)
    implementation(libs.spring.modulith.core)
    implementation(libs.spring.modulith.events)

    // Reactor Netty (embedded HTTP server - replaces Spring Boot's autoconfigured Netty)
    implementation(libs.reactor.netty.http)

    // Spring Data MongoDB Reactive TODO Add MongoDB
    // Spring Data MongoDB Reactive
    implementation(libs.mongodb.driver.reactivestreams)
    implementation(libs.spring.data.mongodb)
    implementation(libs.mongodb.driver.core)
    //implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Spring Data MongoDB Reactive TODO

    // Spring Kafka
    implementation(libs.spring.kafka)
    implementation(libs.reactor.kafka)

    // Telegram Bot Client
    implementation(libs.bundles.telegram.bot)

    // Jackson
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.parameter.names)

    // Logging
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.test)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Json parsing
    implementation(libs.jsoup)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
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

tasks.shadowJar {
    archiveClassifier = ""
    manifest {
        attributes["Main-Class"] = "ru.spbstu.Application"
    }
    mergeServiceFiles()
}

// Задача для запуска LLM-теста отдельно от основного приложения
tasks.register<JavaExec>("runLlmTest") {
    group = "application"
    mainClass = "ru.spbstu.llmsolver.test.LlmTestRunner"
    classpath = sourceSets.main.get().runtimeClasspath
}