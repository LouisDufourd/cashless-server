package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.entities.UserEntity
import fr.plaglefleau.database.exceptions.NotFoundException


interface IUserRepository {

    /**
     * Retrieves a user by its internal identifier.
     *
     * @param userId the user id to search for
     * @return the matching user, or `null` if no user exists with this id
     * @throws NotFoundException if no user exists with this id
     */
    fun getUser(userId: Int): UserEntity

    /**
     * Retrieves a user by its username.
     *
     * @param username the username to search for
     * @return the matching user, or `null` if no user exists with this username
     * @throws NotFoundException if no user exists with this username
     */
    fun getUser(username: String): UserEntity
}