package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object UsersTable : IntIdTable("users") {
    // Public username used for login and identification.
    // The unique index prevents duplicate usernames in the table.
    val username = varchar("username", 45).uniqueIndex()

    // Hashed password value stored in the database.
    // The application should never store plain-text passwords.
    val password = varchar("password", 255)
}
