package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    // Companion object gives Exposed the table-to-entity mapping.
    companion object : IntEntityClass<UserEntity>(UsersTable)

    // Entity property backed by the username column.
    var username by UsersTable.username

    // Entity property backed by the password column.
    var password by UsersTable.password
}