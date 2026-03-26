package fr.plaglefleau.database

import fr.plaglefleau.Config
import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource

object DatabaseFactory {
    // Single shared datasource used by the whole application.
    // This object stores the PostgreSQL connection settings and is reused everywhere.
    val dataSource: PGSimpleDataSource = PGSimpleDataSource()

    fun init(environment: ApplicationEnvironment) {
        // Read database connection settings from the application configuration.
        // This keeps credentials and host information outside the source code.
        val config = Config.databaseConfig(environment)

        // Configure the PostgreSQL datasource with the values from config.
        // These settings tell Exposed where the database lives and how to authenticate.
        dataSource.databaseName = config.database
        dataSource.user = config.user
        dataSource.password = config.password
        dataSource.serverNames = arrayOf(config.host)
        dataSource.portNumbers = intArrayOf(config.port)

        Database.connect(dataSource)
    }

    // Small helper that opens an Exposed transaction and runs the provided block inside it.
    // All database reads and writes should happen inside transaction blocks.
    fun <T> dbQuery(block: () -> T): T = transaction {
        block()
    }
}