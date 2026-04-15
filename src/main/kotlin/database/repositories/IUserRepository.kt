package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.entities.UserEntity

interface IUserRepository {
    fun getUser(userId: Int): UserEntity?
    fun getUser(username: String): UserEntity?
}