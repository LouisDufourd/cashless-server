package fr.plaglefleau

import fr.plaglefleau.api.ExceptionHandler
import fr.plaglefleau.api.receive.*
import fr.plaglefleau.api.response.ErrorMessage
import fr.plaglefleau.api.response.SuccessMessage
import fr.plaglefleau.api.validation.CardValidation
import fr.plaglefleau.database.repositories.postgresql.CardRepository
import fr.plaglefleau.database.repositories.postgresql.TokenSessionRepository
import fr.plaglefleau.database.repositories.postgresql.TransactionLogRepository
import fr.plaglefleau.database.repositories.postgresql.VolunteerRepository
import fr.plaglefleau.database.tables.RoleName
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import java.util.*

/**
 * Configures all HTTP routes for the application.
 *
 * This includes authentication routes, card operations, and helper endpoints
 * that rely on repository access and authorization checks.
 */
fun Application.configureRouting() {
    // Create repository instances once and reuse them across all requests.
    val tokenSessionRepository = TokenSessionRepository()
    val volunteerRepository = VolunteerRepository()
    val transactionLogRepository = TransactionLogRepository()
    val cardRepository = CardRepository()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        swaggerUI("/swaggerUI/generated") {
            info = OpenApiInfo("My API", "1.0")
            source = OpenApiDocSource.Routing(ContentType.Application.Json) {
                routingRoot.descendants()
            }
        }

        swaggerUI(path = "/swagger", swaggerFile = "openapi/documentation.yaml.json") {
            version = "4.15.5"
        }

        authenticate("SELLER", "MANAGER", "RECHARGE") {
            get("/protected") {
                val principal = call.principal<JWTPrincipal>()
                if (principal == null) {
                    sendError(
                        message = "No credentials provided",
                        code = 401,
                        status = HttpStatusCode.Unauthorized
                    )
                    return@get
                }

                val userId = principal.payload.subject
                call.respondText("Token is valid for user $userId")
            }
        }

        route("/api") {
            route("/v1") {
                route("/auth") {
                    post("/login") {
                        val login = call.receive<ReceiveVolunteerLogin>()
                        val id: Int = try {
                            volunteerRepository.login(login.username, login.password)
                        } catch (e: Exception) {
                            val handled = ExceptionHandler.handleException(e)
                            sendError(handled.message, handled.code, handled.status)
                            return@post
                        }

                        val config = Config.jwtConfig(environment)

                        val refreshJti = UUID.randomUUID().toString()
                        val refreshExpiration = System.currentTimeMillis() + config.refreshExpiration * 1000

                        tokenSessionRepository.createTokenSession(
                            volunteerId = id,
                            refreshJti = refreshJti,
                            expiration = refreshExpiration
                        )

                        val accessToken = generateToken(
                            volunteerId = id.toString(),
                            role = volunteerRepository.getRole(id),
                            tokenUUID = UUID.randomUUID().toString(),
                            expirationDate = System.currentTimeMillis() + config.expiration * 1000
                        )

                        val refreshToken = generateRefreshToken(
                            volunteerId = id.toString(),
                            refreshTokenJTI = refreshJti,
                            expirationDate = refreshExpiration
                        )

                        call.respond(
                            status = HttpStatusCode.OK,
                            message = mapOf("accessToken" to accessToken, "refreshToken" to refreshToken)
                        )
                    }

                    authenticate("refresh") {
                        post("/refresh") {
                            val principal = call.principal<JWTPrincipal>()
                            if (principal == null) {
                                sendError(
                                    message = "No credentials provided",
                                    code = 401,
                                    status = HttpStatusCode.Unauthorized
                                )
                                return@post
                            }

                            val id = principal.payload.subject

                            tokenSessionRepository.revokeTokenSession(principal.jwtId!!)

                            val newRefreshJti = UUID.randomUUID().toString()

                            tokenSessionRepository.createTokenSession(
                                volunteerId = id.toInt(),
                                refreshJti = newRefreshJti,
                                expiration = System.currentTimeMillis() + Config.jwtConfig(environment).refreshExpiration * 1000
                            )

                            val newAccessToken = generateToken(
                                volunteerId = id,
                                role = volunteerRepository.getRole(id.toInt()),
                                tokenUUID = UUID.randomUUID().toString(),
                                expirationDate = System.currentTimeMillis() + Config.jwtConfig(environment).expiration * 1000
                            )

                            val newRefreshToken = generateRefreshToken(
                                volunteerId = id,
                                refreshTokenJTI = newRefreshJti,
                                expirationDate = System.currentTimeMillis() + Config.jwtConfig(environment).refreshExpiration * 1000
                            )

                            call.respond(
                                status = HttpStatusCode.OK,
                                message = mapOf("token" to newAccessToken, "refreshToken" to newRefreshToken)
                            )
                        }
                    }
                }

                route("/cards") {
                    route("/{identifier}") {
                        authenticate("ORGANIZER", "SELLER", "MANAGER", "RECHARGE") {
                            get("/history") {
                                val page = call.parameters["page"]?.toIntOrNull()
                                val pageSize = call.parameters["size"]?.toIntOrNull()
                                val identifierValue = call.parameters["identifier"]!!

                                val identifier: Any = identifierValue.toIntOrNull() ?: identifierValue

                                val transactionLog = try {
                                    when (identifier) {
                                        is Int -> transactionLogRepository.getCardTransactionLog(
                                            volunteerIdentifier = identifier,
                                            page ?: 1,
                                            pageSize ?: 10
                                        )

                                        is String -> transactionLogRepository.getCardTransactionLog(
                                            volunteerIdentifier = identifier,
                                            page ?: 1,
                                            pageSize ?: 10
                                        )

                                        else -> {
                                            sendError(
                                                "Unexpected identifier types: ${identifier::class}",
                                                400,
                                                HttpStatusCode.BadRequest
                                            )
                                            return@get
                                        }
                                    }
                                } catch (e: Exception) {
                                    val handled = ExceptionHandler.handleException(e)
                                    sendError(handled.message, handled.code, handled.status)
                                    return@get
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = mapOf(
                                        "transactions" to transactionLog,
                                        "pagination" to mapOf(
                                            "page" to (page ?: 1),
                                            "size" to (pageSize ?: 10)
                                        )
                                    )
                                )
                            }

                            get("/balance") {
                                val identifierValue = call.parameters["identifier"]!!

                                val identifier: Any = identifierValue.toIntOrNull() ?: identifierValue

                                val balance = try {
                                    when (identifier) {
                                        is Int -> cardRepository.getBalance(identifier)
                                        is String -> cardRepository.getBalance(identifier)
                                        else -> {
                                            sendError(
                                                "Unexpected identifier types: ${identifier::class}",
                                                400,
                                                HttpStatusCode.BadRequest
                                            )
                                            return@get
                                        }
                                    }
                                } catch (e: Exception) {
                                    val handled = ExceptionHandler.handleException(e)
                                    sendError(handled.message, handled.code, handled.status)
                                    return@get
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = mapOf("balance" to "$balance€")
                                )
                            }
                        }

                        authenticate("SELLER", "MANAGER") {
                            put("/debit") {
                                val debitRequest = call.receive<ReceiveDebitCard>()
                                val identifierValue = call.parameters["identifier"]!!

                                val identifier: Any = identifierValue.toIntOrNull() ?: identifierValue

                                if (CardValidation.verifyInvalidAmount(debitRequest.amount)) {
                                    sendError(
                                        message = "The amount needs to be positive",
                                        code = 400,
                                        status = HttpStatusCode.BadRequest
                                    )
                                    return@put
                                }

                                try {
                                    when (identifier) {
                                        is Int -> cardRepository.debit(
                                            identifier,
                                            debitRequest.amount,
                                            debitRequest.standName
                                        )

                                        is String -> cardRepository.debit(
                                            identifier,
                                            debitRequest.amount,
                                            debitRequest.standName
                                        )
                                    }
                                } catch (e: Exception) {
                                    /**
                                     * TODO:
                                     *  1. Verify that the card exists
                                     *  2. Verify that the stand exists
                                     *  3. Verify that the card can be debited
                                     */
                                    val handled = ExceptionHandler.handleException(e)
                                    sendError(handled.message, handled.code, handled.status)
                                    return@put
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = SuccessMessage(
                                        message = "The user has been successfully charged",
                                        code = 200
                                    )
                                )
                            }
                        }

                        authenticate("RECHARGE") {
                            put("/credit") {
                                val receiveCardCredit = call.receive<ReceiveCreditCard>()
                                val identifierParam = call.parameters["identifier"]!!

                                val identifier: Any = identifierParam.toIntOrNull() ?: identifierParam

                                if (CardValidation.verifyInvalidAmount(receiveCardCredit.amount)) {
                                    sendError(
                                        message = "The amount needs to be positive",
                                        code = 400,
                                        status = HttpStatusCode.BadRequest
                                    )
                                    return@put
                                }

                                try {
                                    when (identifier) {
                                        is Int -> cardRepository.credit(identifier, receiveCardCredit.amount)
                                        is String -> cardRepository.credit(identifier, receiveCardCredit.amount)
                                    }
                                } catch (e: Exception) {
                                    val handled = ExceptionHandler.handleException(e)
                                    sendError(handled.message, handled.code, handled.status)
                                    return@put
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = SuccessMessage(
                                        message = "The user has been successfully credited",
                                        code = 200
                                    )
                                )
                            }
                        }

                        authenticate("ORGANIZER") {
                            put {
                                val receiveUpdateCard = call.receive<ReceiveUpdateCard>()
                                val identifierParam = call.parameters["identifier"]!!

                                val identifier: Any = identifierParam.toIntOrNull() ?: identifierParam

                                /**
                                 * TODO:
                                 *  - Verify that the card exists
                                 *  - Verify the amount is valid
                                 */

                                when (identifier) {
                                    is Int -> cardRepository.update(
                                        identifier,
                                        receiveUpdateCard.pin,
                                        receiveUpdateCard.amount
                                    )

                                    is String -> cardRepository.update(
                                        identifier,
                                        receiveUpdateCard.pin,
                                        receiveUpdateCard.amount
                                    )
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = SuccessMessage(
                                        "The card `$identifier` has been updated successfully",
                                        200
                                    )
                                )
                            }
                        }
                    }

                    authenticate("ORGANIZER") {
                        post {
                            val receiveCreateCard = call.receive<ReceiveCreateCard>()

                            /**
                             * TODO:
                             *  - Verify the NFC code is valid
                             *  - Verify the pin is valid
                             */

                            try {
                                cardRepository.create(receiveCreateCard.pin, receiveCreateCard.nfcCode)
                            } catch (e: Exception) {
                                val handled = ExceptionHandler.handleException(e)
                                sendError(handled.message, handled.code, handled.status)
                                return@post
                            }

                            call.respond(
                                status = HttpStatusCode.Created,
                                message = SuccessMessage(
                                    message = "The cards was created with success",
                                    code = 201
                                )
                            )
                        }
                    }
                }

                route("/stands") {
                    route("/{id}") {
                        authenticate("ORGANIZER", "SELLER", "MANAGER", "RECHARGE") {
                            get {
                                // TODO: return stand details.
                            }
                        }

                        authenticate("ORGANIZER") {
                            delete {
                                // TODO: delete the stand.
                            }
                        }

                        authenticate("ORGANIZER", "MANAGER") {
                            route("/volunteers") {
                                post {
                                    // TODO: add a volunteer to the stand.
                                }
                            }
                        }

                        authenticate("MANAGER") {
                            delete("/{volunteerId}") {
                                // TODO: remove a volunteer from the stand.
                            }
                        }
                    }

                    route("/inventory") {
                        authenticate("MANAGER", "SELLER") {
                            get {
                                // TODO: list inventory for this stand.
                            }

                            get("/{articleId}") {
                                // TODO: get a specific inventory item by article.
                            }

                            post {
                                // TODO: add inventory item to the stand.
                            }

                            put {
                                // TODO: set inventory quantity and price.
                            }
                        }
                        authenticate("MANAGER") {
                            delete("/{articleId}") {
                                // TODO: remove inventory item.
                            }
                        }
                    }
                }

                authenticate("ORGANIZER") {
                    post {
                        // TODO: create a new stand.
                    }
                }

                authenticate("MANAGER") {
                    put {
                        // TODO: update stand data.
                    }
                }
            }

            route("/volunteers") {
                authenticate("ORGANIZER", "MANAGER") {
                    post {
                        // TODO: create a volunteer.
                    }
                }

                authenticate("ORGANIZER") {
                    put {
                        // TODO: update a volunteer.
                    }

                    delete {
                        // TODO: delete a volunteer.
                    }
                }
            }
        }
    }
}

/**
 * Sends a structured JSON error response.
 *
 * @param message human-readable error message
 * @param code application-specific error code
 * @param status HTTP status to return
 */
private suspend fun RoutingContext.sendError(message: String, code: Int, status: HttpStatusCode) {
    call.respond(
        status = status,
        message = ErrorMessage(
            message = message,
            code = code
        )
    )
}

/**
 * Extracts the authenticated volunteer id from the current JWT principal.
 *
 * If the principal is missing or its subject cannot be parsed as an integer,
 * a `404 Not Found` response is sent and `null` is returned.
 *
 * @return the authenticated volunteer id, or `null` if it cannot be resolved
 */
private suspend fun RoutingContext.getAuthenticatedVolunteerIdOrSendError(): Int? {
    val volunteerId = call.principal<JWTPrincipal>()?.subject?.toIntOrNull()

    if (volunteerId == null) {
        sendError(
            status = HttpStatusCode.NotFound,
            message = "Unable to extract the volunteer id from the authorization",
            code = 404
        )
        return null
    }

    return volunteerId
}

/**
 * Resolves the current volunteer role from the JWT principal.
 *
 * If the role cannot be found, a `401 Unauthorized` response is sent.
 *
 * @param volunteerRepository repository used to read the volunteer role
 * @return the volunteer role, or `null` if it cannot be resolved
 */
private suspend fun RoutingContext.getVolunteerRole(volunteerRepository: VolunteerRepository): RoleName? {
    try {
        val principal = call.principal<JWTPrincipal>() ?: return null
        val volunteerId = principal.payload.subject.toInt()
        val volunteerRole = volunteerRepository.getRole(volunteerId)
        return volunteerRole
    } catch (e: Exception) {
        val handled = ExceptionHandler.handleException(e)
        sendError(handled.message, handled.code, handled.status)
        return null
    }
}

/**
 * Dispatches [CardRepository.connect] to the correct overload based on runtime argument types.
 *
 * Both identifiers may be either an [Int] or a [String], which results in four valid combinations.
 *
 * @param card card identifier, either id or NFC code
 * @param user user identifier, either id or username
 */
private fun CardRepository.connect(card: Any, user: Any) = when (card) {
    is Int if user is Int -> connect(card, user)
    is Int if user is String -> connect(card, user)
    is String if user is Int -> connect(card, user)
    is String if user is String -> connect(card, user)
    else -> error("Unexpected identifier types: ${card::class} / ${user::class}")
}