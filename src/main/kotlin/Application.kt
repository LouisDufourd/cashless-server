package fr.plaglefleau

import fr.plaglefleau.database.DatabaseFactory
import io.ktor.server.application.*

fun main(args: Array<String>) {
    // Standard Ktor entry point for the Netty server.
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Initialize database configuration before any route or security code tries to use it.
    DatabaseFactory.init(environment)

    // Register all major application modules in the correct startup order.
    configureSecurity()
    configureSerialization()
    configureRouting()
}
