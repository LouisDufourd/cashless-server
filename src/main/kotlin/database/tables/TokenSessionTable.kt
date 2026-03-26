package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object TokenSessionTable : IntIdTable("token_session") {
    // Volunteer owning this refresh-session record.
    val volunteerId = integer("volunteer_id")

    // Unique JWT identifier of the refresh token.
    // Used to match a token against the database session.
    val refreshJti = varchar("refresh_jti", 255).uniqueIndex()

    // Soft-revocation flag.
    // If true, the refresh token can no longer be used.
    val revoked = bool("revoked").default(false)

    // Creation timestamp for the token session.
    val createdAt = timestamp("created_at").default(Clock.System.now())

    // Expiration timestamp for the refresh session.
    val expireAt = timestamp("expire_at")
}