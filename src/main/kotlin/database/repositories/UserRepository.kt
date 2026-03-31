package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.UserEntity
import fr.plaglefleau.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.eq

class UserRepository {
    fun getUser(userId: Int): UserEntity? = dbQuery {
        UserEntity.findById(userId)
    }

    fun getUser(username: String): UserEntity? = dbQuery {
        UserEntity.find(UsersTable.username eq username).firstOrNull()
    }
}