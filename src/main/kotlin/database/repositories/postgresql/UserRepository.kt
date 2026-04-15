package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.UserEntity
import fr.plaglefleau.database.repositories.IUserRepository
import fr.plaglefleau.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.eq

class UserRepository: IUserRepository {
    override fun getUser(userId: Int): UserEntity? = dbQuery {
        UserEntity.findById(userId)
    }

    override fun getUser(username: String): UserEntity? = dbQuery {
        UserEntity.find(UsersTable.username eq username).firstOrNull()
    }
}