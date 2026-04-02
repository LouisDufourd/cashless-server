package fr.plaglefleau

import fr.plaglefleau.api.receive.ReceiveDebitCard
import fr.plaglefleau.api.receive.ReceiveConnectCardUser
import fr.plaglefleau.api.receive.ReceiveCreateCard
import fr.plaglefleau.api.receive.ReceiveCreditCard
import fr.plaglefleau.api.receive.ReceiveUpdateCard
import fr.plaglefleau.database.repositories.TokenSessionRepository
import fr.plaglefleau.database.repositories.TransactionLogRepository
import fr.plaglefleau.database.repositories.VolunteerRepository
import fr.plaglefleau.api.response.ErrorMessage
import fr.plaglefleau.api.receive.ReceiveVolunteerLogin
import fr.plaglefleau.api.response.SuccessMessage
import fr.plaglefleau.database.repositories.CardRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

/**
 * TODO vérification de la fonctionnalité des routes:
 *  - api/v1/cards/{identifier}/credit
 *  - api/v1/cards/{identifier}/debit
 * TODO création des routes de création et de modification de carte
 */

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
    val cardRepository = CardRepository()

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
                    sendError(
                        message = "No credentials provided",
                        code = 401,
                        status = HttpStatusCode.Unauthorized
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
                            sendError(
                                message = "Invalid credentials",
                                code = 401,
                                status = HttpStatusCode.Unauthorized
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
                                sendError(
                                    message = "No credentials provided",
                                    code = 401,
                                    status = HttpStatusCode.Unauthorized
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

                        // Card routes by identifier.
                        route("/{identifier}") {
                            get("/history") {
                                // Fetch card history by internal database ID.
                                val page = call.parameters["page"]?.toIntOrNull()
                                val size = call.parameters["size"]?.toIntOrNull()
                                val nfc = call.parameters["identifier"]!!

                                val identifier: Any = nfc.toIntOrNull() ?: nfc

                                val transactionLog = when (identifier) {
                                    is Int -> transactionLogRepository.getCardTransactionLog(identifier, page, pageSize = size)
                                    is String -> transactionLogRepository.getCardTransactionLog(identifier, page, pageSize = size)
                                    else -> error("Unexpected identifier types: ${identifier::class}")
                                }

                                call.respond(
                                    HttpStatusCode.OK,
                                    mapOf(
                                        "transactions" to transactionLog,
                                        "pagination" to mapOf(
                                            "page" to (page ?: 1),
                                            "size" to (size ?: 10)
                                        )
                                    )
                                )
                            }

                            get("/balance") {
                                // Return the current balance for this card.
                                val nfc = call.parameters["identifier"]!!

                                val identifier: Any = nfc.toIntOrNull() ?: nfc

                                val balance = when (identifier) {
                                    is Int -> cardRepository.getBalance(identifier)
                                    is String -> cardRepository.getBalance(identifier)
                                    else -> error("Unexpected identifier types: ${identifier::class}")
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = mapOf(
                                        "balance" to "$balance€"
                                    )
                                )
                            }

                            put("/connect") {
                                // Connect a card to a user.
                                val receiveConnectCardUser = call.receive<ReceiveConnectCardUser>()
                                val nfcCode = call.parameters["identifier"]!!
                                val extractId = nfcCode.toIntOrNull()

                                if (receiveConnectCardUser.userId == null && receiveConnectCardUser.username == null) {
                                    sendError(
                                        message = "You need to specify either the user id or the username",
                                        code = 400,
                                        status = HttpStatusCode.BadRequest
                                    )
                                }

                                val card: Any = extractId ?: nfcCode
                                val user: Any = receiveConnectCardUser.userId ?: receiveConnectCardUser.username!!

                                /**
                                 * TODO verifications:
                                 *  - La carte existe
                                 *  - L'utilisateur existe
                                 *  - La carte n'est pas déjà connecter à un utilisateur
                                 */

                                cardRepository.connect(card, user)

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = SuccessMessage(
                                        message = "The user `$user` has been successfully added to the card `$card`",
                                        code = 200
                                    )
                                )
                            }

                            put("/debit") {
                                // Subtract money from a card balance.
                                val receiveCardDebit = call.receive<ReceiveDebitCard>()

                                val nfc = call.parameters["identifier"]!!

                                val identifier: Any = nfc.toIntOrNull() ?: nfc

                                /**
                                 * TODO verifications:
                                 *  - assez d'argent
                                 *  - amount positifs
                                 *  - la carte existe
                                 *  - verifier que le bénévolent à le droit de débiter
                                 */

                                when (identifier) {
                                    is Int -> cardRepository.debit(identifier, receiveCardDebit.amount, receiveCardDebit.standName)
                                    is String -> cardRepository.debit(identifier, receiveCardDebit.amount, receiveCardDebit.standName)
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = SuccessMessage(
                                        message = "The user has been successfully charged",
                                        code = 200
                                    )
                                )
                            }

                            put("/credit") {
                                // Add money to a card balance.
                                val receiveCardCredit = call.receive<ReceiveCreditCard>()

                                val nfc = call.parameters["identifier"]!!

                                val identifier: Any = nfc.toIntOrNull() ?: nfc

                                /**
                                 * TODO verification:
                                 *  - vérifier que le bénévolent à le droit de débiter
                                 *  - vérifier que amount est positif
                                 *  - vérifier que la carte existe
                                 *  - vérifier que le stand existe
                                 *  - vérifier que la carte a les moyens de payer
                                 */

                                when (identifier) {
                                    is Int -> cardRepository.credit(identifier, receiveCardCredit.amount)
                                    is String -> cardRepository.credit(identifier, receiveCardCredit.amount)
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = SuccessMessage(
                                        message = "The user has been successfully credited",
                                        code = 200
                                    )
                                )
                            }

                            put {
                                // Update card information.
                                val receiveUpdateCard = call.receive<ReceiveUpdateCard>()

                                val nfc = call.parameters["identifier"]!!

                                val identifier: Any = nfc.toIntOrNull() ?: nfc

                                when(identifier) {
                                    is Int -> cardRepository.update(identifier, receiveUpdateCard.pin, receiveUpdateCard.amount)
                                    is String -> cardRepository.update(identifier, receiveUpdateCard.pin, receiveUpdateCard.amount)
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    SuccessMessage(
                                        "The card `$identifier` has been updated successfully",
                                        200
                                    )
                                )
                            }
                        }

                        post {
                            // Create a new card.
                            val receiveCreateCard = call.receive<ReceiveCreateCard>()

                            cardRepository.create(receiveCreateCard.pin, receiveCreateCard.nfcCode)

                            /**
                             * TODO verifications:
                             *  - Verifier que le bénévolent à les permissions
                             *  - Vérifier que la carte n'existe pas déjà
                             */

                            call.respond(
                                HttpStatusCode.Created,
                                message = SuccessMessage(
                                    message = "The cards was created with success",
                                    code = 201
                                )
                            )
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

                                put {
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

private suspend fun RoutingContext.sendError(message: String, code: Int, status: HttpStatusCode) {
    call.respond(
        status = status,
        message = ErrorMessage(
            message = message,
            code = code
        )
    )
}

private fun CardRepository.connect(card: Any, user: Any) = when {
    card is Int && user is Int -> connect(card, user)
    card is Int && user is String -> connect(card, user)
    card is String && user is Int -> connect(card, user)
    card is String && user is String -> connect(card, user)
    else -> error("Unexpected identifier types: ${card::class} / ${user::class}")
}
