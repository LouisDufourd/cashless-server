package fr.plaglefleau.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource
import java.io.File

object DatabaseFactory {
    // Single shared datasource used by the whole application.
    // This object stores the PostgreSQL connection settings and is reused everywhere.
    val dataSource: PGSimpleDataSource = PGSimpleDataSource()

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

    // Small helper that opens an Exposed transaction and runs the provided block inside it.
    // All database reads and writes should happen inside transaction blocks.
    fun <T> dbQuery(block: () -> T): T = transaction {
        block()
    }

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