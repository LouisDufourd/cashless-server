package fr.plaglefleau.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource
import java.io.File

/**
 * Central entry point for database configuration and transaction handling.
 *
 * This object owns the shared PostgreSQL data source and provides small helpers
 * for opening transactions and running SQL seed files.
 */
object DatabaseFactory {
    // Single shared datasource used by the whole application.
    // This object stores the PostgreSQL connection settings and is reused everywhere.
    val dataSource: PGSimpleDataSource = PGSimpleDataSource()

    /**
     * Initializes the PostgreSQL connection used by Exposed.
     *
     * This configures the shared datasource and binds it to Exposed so that all
     * later repository calls can run inside transactions.
     *
     * @param database database name to connect to
     * @param user database username
     * @param password database password
     * @param serverNames PostgreSQL host names
     * @param portNumber PostgreSQL port numbers
     */
    fun init(database: String, user: String, password: String, serverNames: Array<String>, portNumber: IntArray) {
        // Configure the PostgreSQL datasource with the values from config.
        // These settings tell Exposed where the database lives and how to authenticate.
        dataSource.databaseName = database
        dataSource.user = user
        dataSource.password = password
        dataSource.serverNames = serverNames
        dataSource.portNumbers = portNumber

        Database.connect(dataSource)
    }

    /**
     * Runs the provided block inside an Exposed transaction.
     *
     * Use this helper for all database reads and writes so that connection
     * management stays consistent across the project.
     *
     * @param block code to execute in a transaction
     * @return the result produced by [block]
     */
    fun <T> dbQuery(block: () -> T): T = transaction {
        block()
    }

    /**
     * Executes a SQL file by splitting it into individual statements.
     *
     * The file is read from disk, split on semicolons, and each non-blank statement
     * is executed in a single transaction.
     *
     * @param path path to the SQL file to execute
     */
    fun executeSqlFile(path: String) {
        val statements = File(path)
            .readText()
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        transaction {
            for (sql in statements) {
                exec(sql)
            }
        }
    }
}