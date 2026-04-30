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

    // Spring Kafka
    implementation(libs.spring.kafka)
    implementation(libs.reactor.kafka)

    // Telegram Bot Client
    implementation(libs.bundles.telegram.bot)

    // Jackson
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.parameter.names)
    // SpringDoc
    //implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")

    // Logging
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    // Json parsing
    implementation(libs.jsoup)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)


    // Testing
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.test)
    testRuntimeOnly(libs.junit.platform.launcher)
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
