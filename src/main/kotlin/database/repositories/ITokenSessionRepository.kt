package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.TokenSessionDTO
import fr.plaglefleau.database.entities.TokenSessionEntity

interface ITokenSessionRepository {
    fun findActiveTokenSession(refreshJti: String): TokenSessionDTO?
    fun revokeTokenSession(refreshJti: String)
    fun createTokenSession(volunteerId: Int, refreshJti: String, expiration: Long)
}