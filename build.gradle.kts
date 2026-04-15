val kotlin_version: String by project
val logback_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.1"
}

group = "fr.plaglefleau"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-gson")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-auth:${ktor_version}")
    implementation("io.ktor:ktor-server-auth-jwt:${ktor_version}")

    implementation("org.jetbrains.exposed:exposed-core:1.1.1")
    implementation("org.jetbrains.exposed:exposed-dao:1.1.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.1.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.1.1")
    implementation("org.postgresql:postgresql:42.7.10")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.3.20")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}
val testDbServiceName = "test-db"
val testSqlFile = "docker/db/testDataGeneration.sql"

tasks.register<Exec>("seedTestDb") {
    group = "verification"
    description = "Loads test data into the Docker test database"

    dependsOn("waitForTestDb")

    commandLine(
        "docker", "compose", "exec", "-T",
        testDbServiceName,
        "psql",
        "-U", "cashless_user-test",
        "-d", "cashless-test",
        "-f", "/docker-entrypoint-initdb.d/testDataGeneration.sql"
    )
}

tasks.register<Exec>("startTestDb") {
    group = "verification"
    description = "Starts the Docker test database"

    commandLine("docker", "compose", "up", "-d", testDbServiceName)
}

tasks.register("waitForTestDb") {
    group = "verification"
    description = "Waits until the Docker test database is healthy"

    dependsOn("startTestDb")

    doLast {
        val maxAttempts = 30
        var attempt = 0
        var healthy = false

        while (attempt < maxAttempts && !healthy) {
            attempt++

            val process = ProcessBuilder(
                "docker", "compose", "ps", "--format", "json", testDbServiceName
            ).redirectErrorStream(true).start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            healthy = output.lowercase().contains("healthy")

            if (!healthy) {
                Thread.sleep(2000)
            }
        }

        if (!healthy) {
            throw GradleException("Test database did not become healthy in time.")
        }
    }
}

tasks.register<Exec>("stopTestDb") {
    group = "verification"
    description = "Stops the Docker test database"

    commandLine("docker", "compose", "stop", testDbServiceName)
}

tasks.test {
    dependsOn("seedTestDb")
    finalizedBy("stopTestDb")

    useJUnitPlatform()

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    systemProperty("DB_HOST", "localhost")
    systemProperty("DB_PORT", "4062")
    systemProperty("DB_NAME", "cashless-test")
    systemProperty("DB_USER", "cashless_user-test")
    systemProperty("DB_PASSWORD", "cashless_password-test")
}
