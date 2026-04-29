package fr.plaglefleau

import fr.plaglefleau.database.DatabaseFactory
import io.ktor.server.application.*

fun main(args: Array<String>) {
    // Standard Ktor entry point for the Netty server.
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Initialize database configuration before any route or security code tries to use it.
    // Read database connection settings from the application configuration.
    // This keeps credentials and host information outside the source code.
    val config = Config.databaseConfig(environment)

    DatabaseFactory.init(
        config.database,
        config.user,
        config.password,
        serverNames = arrayOf(config.host),
        portNumber = intArrayOf(config.port)
    )

    // Register all major application modules in the correct startup order.
    configureSecurity()
    configureSerialization()
    configureRouting()
    configureCORS()
}
