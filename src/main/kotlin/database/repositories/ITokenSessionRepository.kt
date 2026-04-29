package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.TokenSessionDTO
import fr.plaglefleau.database.exceptions.NotFoundException

/**
 * Defines persistence operations for refresh token sessions.
 *
 * Implementations manage the lifecycle of token sessions used for authentication,
 * including lookup, creation, and revocation of refresh tokens.
 */
interface ITokenSessionRepository {

    /**
     * Finds an active token session by its refresh token identifier.
     *
     * @param refreshJti the unique identifier of the refresh token
     * @return the active token session associated with the given identifier
     * @throws NotFoundException if no active session exists with this identifier
     */
    fun findActiveTokenSession(refreshJti: String): TokenSessionDTO

    /**
     * Revokes a token session by its refresh token identifier.
     *
     * After revocation, the refresh token should no longer be accepted.
     *
     * @param refreshJti the unique identifier of the refresh token to revoke
     */
    fun revokeTokenSession(refreshJti: String)

    /**
     * Creates a new token session for a volunteer.
     *
     * @param volunteerId the id of the volunteer associated with the session
     * @param refreshJti the unique identifier of the refresh token
     * @param expiration the session expiration timestamp
     */
    fun createTokenSession(volunteerId: Int, refreshJti: String, expiration: Long)
}