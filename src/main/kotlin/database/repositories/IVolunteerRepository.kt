package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.tables.RoleName
import fr.plaglefleau.database.exceptions.*

/**
 * Defines persistence operations for volunteer authentication and authorization data.
 *
 * Implementations are responsible for validating volunteer credentials and retrieving
 * the role associated with a volunteer account.
 */
interface IVolunteerRepository {

    /**
     * Authenticates a volunteer with the provided credentials.
     *
     * @param username the volunteer username
     * @param password the volunteer password
     * @return the authenticated volunteer id
     * @throws AuthenticationException if the credentials are invalid
     */
    fun login(username: String, password: String): Int

    /**
     * Retrieves the role assigned to a volunteer.
     *
     * @param volunteerId the volunteer id to inspect
     * @return the volunteer role
     * @throws NotFoundException if no volunteer exists with this id
     */
    fun getRole(volunteerId: Int): RoleName
}