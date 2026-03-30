package fr.plaglefleau

import fr.plaglefleau.database.repositories.TokenSessionRepository
import fr.plaglefleau.database.repositories.TransactionLogRepository
import fr.plaglefleau.database.repositories.VolunteerRepository
import fr.plaglefleau.api.response.ErrorMessage
import fr.plaglefleau.api.receive.ReceiveVolunteerLogin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

/**
 * Defines the HTTP routes for the application.
 *
 * This file connects:
 * - Request parsing
 * - Authentication
 * - Repository access
 * - Token generation
 * - API responses
 *
 * The routes are grouped by domain for readability.
 */
fun Application.configureRouting() {
    // Repositories are created once and reused for all requests.
    // This keeps route handlers simple and avoids rebuilding repository objects repeatedly.
    val tokenSessionRepository = TokenSessionRepository()
    val volunteerRepository = VolunteerRepository()
    val transactionLogRepository = TransactionLogRepository()

    routing {
        // Simple public endpoint used to verify the application is running.
        get("/") {
            call.respondText("Hello World!")
        }

        // Protected endpoint that requires a valid access token.
        authenticate("api") {
            get("/protected") {
                // Read the authenticated JWT principal from the current request.
                // If authentication failed for some reason, the principal will be null.
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    call.respond(
                        status = HttpStatusCode.Unauthorized,
                        message = "error" to " No credentials provided"
                    )
                    return@get
                }

                // The subject contains the volunteer ID stored in the token.
                val userId = principal.payload.subject
                call.respondText("Token is valid for user $userId")
            }
        }

        // Group all API routes under /api/v1.
        route("/api") {
            route("/v1") {
                route("/auth") {

                    // Login endpoint:
                    // - reads username/password from the request body
                    // - checks credentials in the database
                    // - creates a refresh session
                    // - returns both access and refresh tokens
                    post("/login") {
                        val login = call.receive<ReceiveVolunteerLogin>()

                        // Query the database for a volunteer matching the provided credentials.
                        val id: Int? = volunteerRepository.login(login.username, login.password)

                        // Read JWT configuration from application settings.
                        val config = Config.jwtConfig(environment)

                        // If login failed, return a 401 response.
                        if (id == null) {
                            call.respond(
                                status = HttpStatusCode.Unauthorized,
                                message = ErrorMessage(
                                    message = "Invalid credentials",
                                    code = 401
                                )
                            )
                            return@post
                        }

                        // Create a unique ID for the refresh token session.
                        val refreshJti = UUID.randomUUID().toString()

                        // Calculate when the refresh token should expire.
                        val refreshExpiration = System.currentTimeMillis() + config.refreshExpiration * 1000

                        // Save the refresh session in the database.
                        // This allows later validation and revocation.
                        tokenSessionRepository.createTokenSession(
                            volunteerId = id,
                            refreshJti,
                            expiration = refreshExpiration
                        )

                        // Build the access token used for normal API calls.
                        val accessToken = generateToken(
                            volunteerId = id.toString(),
                            tokenUUID = UUID.randomUUID().toString(),
                            expirationDate = System.currentTimeMillis() + config.expiration * 1000
                        )

                        // Build the refresh token tied to the stored session.
                        val refreshToken = generateRefreshToken(
                            volunteerId = id.toString(),
                            refreshTokenJTI = refreshJti,
                            expirationDate = refreshExpiration
                        )

                        // Return both tokens to the client.
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = mapOf("accessToken" to accessToken, "refreshToken" to refreshToken)
                        )
                    }

                    // Refresh endpoint:
                    // - only accepts a valid refresh token
                    // - revokes the old session
                    // - creates a new session
                    // - returns a fresh access/refresh token pair
                    authenticate("refresh") {
                        post("/refresh") {
                            // Read the refresh token principal validated by the auth provider.
                            val principal = call.principal<JWTPrincipal>()
                            if (principal == null) {
                                call.respond(
                                    status = HttpStatusCode.Unauthorized,
                                    message = mapOf("error" to "No credentials provided")
                                )
                                return@post
                            }

                            // Subject contains the volunteer ID.
                            val id = principal.payload.subject

                            // Revoke the old refresh session so the old token cannot be reused.
                            tokenSessionRepository.revokeTokenSession(principal.jwtId!!)

                            // Create a brand-new refresh session identifier.
                            val newRefreshJti = UUID.randomUUID().toString()

                            // Store the new refresh session in the DB.
                            tokenSessionRepository.createTokenSession(
                                volunteerId = id.toInt(),
                                refreshJti = newRefreshJti,
                                expiration = System.currentTimeMillis() + Config.jwtConfig(environment).refreshExpiration * 1000
                            )

                            // Generate a new access token.
                            val newAccessToken = generateToken(
                                volunteerId = id,
                                tokenUUID = UUID.randomUUID().toString(),
                                expirationDate = System.currentTimeMillis() + Config.jwtConfig(environment).expiration * 1000
                            )

                            // Generate the new refresh token tied to the new DB session.
                            val newRefreshToken = generateRefreshToken(
                                volunteerId = id,
                                refreshTokenJTI = newRefreshJti,
                                expirationDate = System.currentTimeMillis() + Config.jwtConfig(environment).refreshExpiration * 1000
                            )

                            // Return the new tokens to the client.
                            call.respond(
                                status = HttpStatusCode.OK,
                                message = mapOf("token" to newAccessToken, "refreshToken" to newRefreshToken)
                            )
                        }
                    }
                }

                // Protected business API routes.
                // These routes require a valid access token.
                authenticate("api") {
                    route("/cards") {

                        // Card routes by numeric ID.
                        route("/{id}") {
                            get("/history/{page}") {
                                // Fetch card history by internal database ID.
                            }

                            get("/balance") {
                                // Return the current balance for this card.
                            }

                            put("/connect") {
                                // Connect a card to a user.
                            }

                            put("/debit") {
                                // Subtract money from a card balance.
                            }

                            put("/credit") {
                                // Add money to a card balance.
                            }

                            delete {
                                // Delete or deactivate the card.
                            }
                        }

                        // Card routes by NFC code.
                        route("/{codeNFC}") {
                            get("/history") {
                                // Fetch card history by NFC code.
                            }

                            get("/balance") {
                                // Return balance by NFC code.
                            }

                            put("/connect") {
                                // Connect NFC card to a user.
                            }

                            put("/debit") {
                                // Debit using NFC identifier.
                            }

                            put("/credit") {
                                // Credit using NFC identifier.
                            }

                            delete {
                                // Remove or deactivate the card by NFC.
                            }
                        }

                        post("/create") {
                            // Create a new card.
                        }

                        put("/update") {
                            // Update card information.
                        }
                    }

                    route("/stands") {
                        route("/{id}") {
                            get {
                                // Return stand details.
                            }

                            delete {
                                // Delete the stand.
                            }

                            route("/volunteers") {
                                post {
                                    // Add a volunteer to the stand.
                                }

                                delete("/{volunteerId}") {
                                    // Remove a volunteer from the stand.
                                }
                            }

                            route("/inventory") {
                                get {
                                    // List inventory for this stand.
                                }

                                get("/{articleId}") {
                                    // Get a specific inventory item by article.
                                }

                                post {
                                    // Add inventory item to the stand.
                                }

                                put("/set") {
                                    // Set inventory quantity/price.
                                }

                                delete("/{articleId}") {
                                    // Remove inventory item.
                                }
                            }
                        }

                        post {
                            // Create a new stand.
                        }

                        put {
                            // Update stand data.
                        }
                    }

                    route("/volunteers") {
                        post {
                            // Create a volunteer.
                        }

                        put {
                            // Update a volunteer.
                        }

                        delete {
                            // Delete a volunteer.
                        }
                    }
                }
            }
        }
    }
}
