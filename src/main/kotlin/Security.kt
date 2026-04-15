package fr.plaglefleau

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.plaglefleau.Config.jwtConfig
import fr.plaglefleau.database.repositories.postgresql.TokenSessionRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

/**
 * Configures JWT authentication for the application.
 *
 * This defines two separate JWT authentication providers:
 * - "Api" for normal access tokens
 * - "Refresh" for refresh tokens
 *
 * Keeping them separate prevents access tokens and refresh tokens from being used interchangeably.
 */
fun Application.configureSecurity() {
    // Repository used to validate, revoke, and create refresh-token sessions in the database.
    // The refresh token is only valid if a matching session exists in the DB.
    val tokenSessionRepository = TokenSessionRepository()

    // Keep the environment reference so JWT secrets and expiration settings can be read from config.
    val env = environment

    // Install Ktor's authentication plugin.
    install(Authentication) {
        jwt("api") {
            // Verifier for access tokens.
            // It checks the JWT signature using the access-token secret.
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig(env).secret))
                    .withAudience("CashlessApi")
                    .build()
            )

            validate { credential ->
                // Reject tokens that do not belong to this application or are already expired.
                // If valid, wrap the token payload in a JWTPrincipal so route handlers can read claims.
                if (
                    credential.payload.audience.contains("CashlessApi")
                    && credential.payload.expiresAt.time > System.currentTimeMillis()
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

        jwt("refresh") {
            // Verifier for refresh tokens.
            // Refresh tokens use a different secret so they cannot be exchanged with access tokens.
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig(env).refreshSecret))
                    .withAudience("CashlessApi")
                    .build()
            )

            validate { credential ->
                // A refresh token is not enough by itself:
                // the application also checks the token session stored in the database.
                val activeTokenSession = tokenSessionRepository.findActiveTokenSession(credential.jwtId!!)

                // Refresh token rules:
                // - token must target this application
                // - token must not be expired
                // - a session must exist in the DB
                // - the DB session must not be revoked
                // - the DB session must belong to the same volunteer as the token subject
                // - the DB session must not be expired
                if (
                    credential.payload.audience.contains("CashlessApi")
                    && credential.payload.expiresAt.time > System.currentTimeMillis()
                    && activeTokenSession != null
                    && !activeTokenSession.revoked
                    && activeTokenSession.volunteer == credential.payload.subject.toInt()
                    && !activeTokenSession.isExpired()
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}

/**
 * Creates a short-lived access token.
 *
 * Access tokens are used for normal API requests.
 * They contain:
 * - The application audience
 * - The volunteer ID as a subject
 * - A unique JWT ID
 * - An expiration date
 * - A claim identifying the token type
 */
fun Application.generateToken(volunteerId: String, tokenUUID: String, expirationDate: Long): String {
    val jwtConfig = jwtConfig(environment)

    return JWT.create()
        // Identify which application this token belongs to.
        .withAudience("CashlessApi")
        // Store the volunteer ID inside the token.
        .withSubject(volunteerId)
        // Set token expiration time.
        .withExpiresAt(Date(expirationDate))
        // Give the token a unique identifier.
        .withJWTId(tokenUUID)
        // Mark this token as an access token.
        .withClaim("typ", "access")
        // Sign with the access-token secret.
        .sign(Algorithm.HMAC256(jwtConfig.secret))
}

/**
 * Creates a refresh token.
 *
 * Refresh tokens are used to get a new access token when the old one expires.
 * This token type is tied to a database session so it can be revoked.
 */
fun Application.generateRefreshToken(volunteerId: String, refreshTokenJTI: String, expirationDate: Long): String {
    val jwtConfig = jwtConfig(environment)

    return JWT.create()
        // Identify the target application.
        .withAudience("CashlessApi")
        // Store the volunteer ID so the server knows who owns the session.
        .withSubject(volunteerId)
        // Set the refresh token expiration time.
        .withExpiresAt(Date(expirationDate))
        // Store the refresh-session identifier.
        .withJWTId(refreshTokenJTI)
        // Mark this token as a refresh token.
        .withClaim("typ", "refresh")
        // Sign with the refresh-token secret.
        .sign(Algorithm.HMAC256(jwtConfig.refreshSecret))
}
