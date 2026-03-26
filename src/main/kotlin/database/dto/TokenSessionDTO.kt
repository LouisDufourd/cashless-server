package fr.plaglefleau.database.dto

import kotlin.time.Instant

data class TokenSessionDTO(
    val id: Int,
    val volunteer: Int,
    val refreshJTI: String,
    val revoked: Boolean,
    val createdAt: Instant,
    val expireAt: Instant
) {
    // Simple helper used during refresh-token validation.
    fun isExpired() = expireAt.toEpochMilliseconds() < System.currentTimeMillis()
}
