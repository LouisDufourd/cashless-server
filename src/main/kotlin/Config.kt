package fr.plaglefleau

import io.ktor.server.application.*

data class JwtConfig(
    val secret: String,
    val expiration: Long,
    val refreshSecret: String,
    val refreshExpiration: Long
)

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String
)

object Config {
    fun databaseConfig(environment: ApplicationEnvironment): DatabaseConfig {
        val config = environment.config
        return DatabaseConfig(
            host = config.property("database.host").getString(),
            port = config.property("database.port").getString().toInt(),
            database = config.property("database.name").getString(),
            user = config.property("database.user").getString(),
            password = config.property("database.password").getString()
        )
    }

    fun jwtConfig(environment: ApplicationEnvironment): JwtConfig {
        val config = environment.config
        return JwtConfig(
            secret = config.property("jwt.secret").getString(),
            expiration = config.property("jwt.expiration").getString().toLong(),
            refreshSecret = config.property("jwt.refreshSecret").getString(),
            refreshExpiration = config.property("jwt.refreshExpiration").getString().toLong(),
        )
    }
}