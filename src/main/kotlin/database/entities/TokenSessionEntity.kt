package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.TokenSessionTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class TokenSessionEntity(id: EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the token_session table.
    companion object : IntEntityClass<TokenSessionEntity>(TokenSessionTable)

    // Owning volunteer reference.
    var volunteerId by TokenSessionTable.volunteerId

    // Refresh token unique identifier.
    var refreshJti by TokenSessionTable.refreshJti

    // Revocation flag for the refresh token session.
    var revoked by TokenSessionTable.revoked

    // Record creation time.
    var createdAt by TokenSessionTable.createdAt

    // Session expiration time.
    var expireAt by TokenSessionTable.expireAt
}