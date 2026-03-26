package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.dto.TokenSessionDTO
import fr.plaglefleau.database.entities.TokenSessionEntity
import fr.plaglefleau.database.entities.VolunteerEntity
import fr.plaglefleau.database.tables.TokenSessionTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Instant

class TokenSessionRepository {
    fun findActiveTokenSession(refreshJti: String) : TokenSessionDTO? = dbQuery {
        // Find a refresh-session row by its JWT identifier.
        TokenSessionEntity.find {
            TokenSessionTable.refreshJti eq refreshJti
        }.map { tokenSessionEntity ->
            TokenSessionDTO(
                id = tokenSessionEntity.id.value,
                volunteer = tokenSessionEntity.volunteerId,
                refreshJTI = tokenSessionEntity.refreshJti,
                revoked = tokenSessionEntity.revoked,
                createdAt = tokenSessionEntity.createdAt,
                expireAt = tokenSessionEntity.expireAt
            )
        }.firstOrNull()
    }

    fun revokeTokenSession(refreshJti: String) = dbQuery {
        // Mark the session as revoked so the refresh token can no longer be reused.
        TokenSessionTable.update(where = {
            TokenSessionTable.refreshJti eq refreshJti
        }) {
            it[revoked] = true
        }
    }

    fun createTokenSession(volunteerId: Int, refreshJti: String, expiration: Long) = dbQuery {
        // Create a new session row tied to the volunteer and refresh token.
        // VolunteerEntity.findById(...) retrieves the owning volunteer before saving the session.
        TokenSessionEntity.new {
            this.volunteerId = VolunteerEntity.findById(volunteerId)!!.id.value
            this.refreshJti = refreshJti
            this.expireAt = Instant.fromEpochMilliseconds(expiration)
        }
    }
}