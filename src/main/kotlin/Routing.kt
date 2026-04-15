package fr.plaglefleau

import fr.plaglefleau.api.receive.ReceiveDebitCard
import fr.plaglefleau.api.receive.ReceiveConnectCardUser
import fr.plaglefleau.api.receive.ReceiveCreateCard
import fr.plaglefleau.api.receive.ReceiveCreditCard
import fr.plaglefleau.api.receive.ReceiveUpdateCard
import fr.plaglefleau.database.repositories.postgresql.TokenSessionRepository
import fr.plaglefleau.database.repositories.postgresql.TransactionLogRepository
import fr.plaglefleau.database.repositories.postgresql.VolunteerRepository
import fr.plaglefleau.api.response.ErrorMessage
import fr.plaglefleau.api.receive.ReceiveVolunteerLogin
import fr.plaglefleau.api.response.SuccessMessage
import fr.plaglefleau.api.validation.CardValidation
import fr.plaglefleau.api.validation.StandValidation
import fr.plaglefleau.database.repositories.postgresql.CardRepository
import fr.plaglefleau.api.validation.VolunteerValidation
import fr.plaglefleau.database.repositories.postgresql.StandRepository
import fr.plaglefleau.database.tables.RoleName
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

    val cardValidation = CardValidation(CardRepository())
    val volunteerValidation = VolunteerValidation(VolunteerRepository())
    val standValidation = StandValidation(StandRepository())

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        authenticate("api") {
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
                        val id: Int? = volunteerRepository.login(login.username, login.password)
                        val config = Config.jwtConfig(environment)

                        if (id == null) {
                            sendError(
                                message = "Invalid credentials",
                                code = 401,
                                status = HttpStatusCode.Unauthorized
                            )
                            return@post
                        }

                        val refreshJti = UUID.randomUUID().toString()
                        val refreshExpiration = System.currentTimeMillis() + config.refreshExpiration * 1000

                        tokenSessionRepository.createTokenSession(
                            volunteerId = id,
                            refreshJti = refreshJti,
                            expiration = refreshExpiration
                        )

                        val accessToken = generateToken(
                            volunteerId = id.toString(),
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

                authenticate("api") {
                    route("/cards") {
                        route("/{identifier}") {
                            get("/history") {
                                val page = call.parameters["page"]?.toIntOrNull()
                                val size = call.parameters["size"]?.toIntOrNull()
                                val identifierValue = call.parameters["identifier"]!!

                                getVolunteerRole(volunteerRepository) ?: return@get

                                val identifier: Any = identifierValue.toIntOrNull() ?: identifierValue

                                val transactionLog = when (identifier) {
                                    is Int -> transactionLogRepository.getCardTransactionLog(identifier, page, pageSize = size)
                                    is String -> transactionLogRepository.getCardTransactionLog(identifier, page, pageSize = size)
                                    else -> error("Unexpected identifier types: ${identifier::class}")
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = mapOf(
                                        "transactions" to transactionLog,
                                        "pagination" to mapOf(
                                            "page" to (page ?: 1),
                                            "size" to (size ?: 10)
                                        )
                                    )
                                )
                            }

                            get("/balance") {
                                val identifierValue = call.parameters["identifier"]!!

                                getVolunteerRole(volunteerRepository) ?: return@get

                                val identifier: Any = identifierValue.toIntOrNull() ?: identifierValue

                                val balance = when (identifier) {
                                    is Int -> cardRepository.getBalance(identifier)
                                    is String -> cardRepository.getBalance(identifier)
                                    else -> error("Unexpected identifier types: ${identifier::class}")
                                }

                                call.respond(
                                    status = HttpStatusCode.OK,
                                    message = mapOf("balance" to "$balance€")
                                )
                            }

                            // Temporary endpoint for connecting a card to a user.
                            put("/connect") {
                                val receiveConnectCardUser = call.receive<ReceiveConnectCardUser>()
                                val nfcCode = call.parameters["identifier"]!!
                                val extractId = nfcCode.toIntOrNull()

                                getVolunteerRole(volunteerRepository) ?: return@put

                                if (receiveConnectCardUser.userId == null && receiveConnectCardUser.username == null) {
                                    sendError(
                                        message = "You need to specify either the user id or the username",
                                        code = 400,
                                        status = HttpStatusCode.BadRequest
                                    )
                                    return@put
                                }

                                val card: Any = extractId ?: nfcCode
                                val user: Any = receiveConnectCardUser.userId ?: receiveConnectCardUser.username!!

                                /**
                                 * Validates that the target card and user exist and that the card is not already linked.
                                 *
                                 * These checks are still marked as TODO because they depend on the repository
                                 * methods that will enforce the final business rules.
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
                                val debitRequest = call.receive<ReceiveDebitCard>()
                                val identifierValue = call.parameters["identifier"] ?: run {
                                    sendError(
                                        message = "Missing card identifier",
                                        code = 400,
                                        status = HttpStatusCode.BadRequest
                                    )
                                    return@put
                                }

                                val volunteerId = getAuthenticatedVolunteerIdOrSendError() ?: return@put
                                requireVolunteerWithoutRolesOrSendError(volunteerId, RoleName.SELLER, RoleName.MANAGER)

                                val identifier: Any = identifierValue.toIntOrNull() ?: identifierValue

                                if (cardValidation.verifyInvalidAmount(debitRequest.amount)) {
                                    sendError(
                                        message = "The amount needs to be positive",
                                        code = 400,
                                        status = HttpStatusCode.BadRequest
                                    )
                                    return@put
                                }

                                if (cardValidation.cardExist(identifier)) {
                                    sendError(
                                        message = "There is no card with the identifier `$identifier`.",
                                        code = 404,
                                        status = HttpStatusCode.NotFound
                                    )
                                    return@put
                                }

                                if (cardValidation.canDebitCard(identifier, debitRequest.amount)) {
                                    sendError(
                                        message = "The card doesn't have enough money to be debited",
                                        code = 409,
                                        status = HttpStatusCode.Conflict
                                    )
                                    return@put
                                }

                                when (identifier) {
                                    is Int -> cardRepository.debit(identifier, debitRequest.amount, debitRequest.standName)
                                    is String -> cardRepository.debit(identifier, debitRequest.amount, debitRequest.standName)
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
                                val receiveCardCredit = call.receive<ReceiveCreditCard>()
                                val identifierParam = call.parameters["identifier"]!!

                                val volunteerId = getAuthenticatedVolunteerIdOrSendError() ?: return@put
                                requireVolunteerWithoutRolesOrSendError(volunteerId, RoleName.RECHARGE)

                                val identifier: Any = identifierParam.toIntOrNull() ?: identifierParam

                                if (cardValidation.cardExist(identifier)) {
                                    sendError(
                                        message = "There is no card with the identifier `$identifier`.",
                                        code = 404,
                                        status = HttpStatusCode.NotFound
                                    )
                                    return@put
                                }

                                if (cardValidation.verifyInvalidAmount(receiveCardCredit.amount)) {
                                    sendError(
                                        message = "The amount needs to be positive",
                                        code = 400,
                                        status = HttpStatusCode.BadRequest
                                    )
                                    return@put
                                }

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
                                val receiveUpdateCard = call.receive<ReceiveUpdateCard>()
                                val identifierParam = call.parameters["identifier"]!!

                                val volunteerId = getAuthenticatedVolunteerIdOrSendError() ?: return@put
                                requireVolunteerWithoutRolesOrSendError(volunteerId, RoleName.ORGANIZER)

                                val identifier: Any = identifierParam.toIntOrNull() ?: identifierParam

                                when (identifier) {
                                    is Int -> cardRepository.update(identifier, receiveUpdateCard.pin, receiveUpdateCard.amount)
                                    is String -> cardRepository.update(identifier, receiveUpdateCard.pin, receiveUpdateCard.amount)
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

                        post {
                            val receiveCreateCard = call.receive<ReceiveCreateCard>()

                            cardRepository.create(receiveCreateCard.pin, receiveCreateCard.nfcCode)

                            val volunteerId = getAuthenticatedVolunteerIdOrSendError() ?: return@post
                            requireVolunteerWithoutRolesOrSendError(volunteerId, RoleName.ORGANIZER)

                            if (cardValidation.cardExist(receiveCreateCard.nfcCode)) {
                                sendError(
                                    status = HttpStatusCode.Conflict,
                                    message = "This card already exist",
                                    code = 409
                                )
                            }

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
                                // TODO: return stand details.
                            }

                            delete {
                                // TODO: delete the stand.
                            }

                            route("/volunteers") {
                                post {
                                    // TODO: add a volunteer to the stand.
                                }

                                delete("/{volunteerId}") {
                                    // TODO: remove a volunteer from the stand.
                                }
                            }

                            route("/inventory") {
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

                                delete("/{articleId}") {
                                    // TODO: remove inventory item.
                                }
                            }
                        }

                        post {
                            // TODO: create a new stand.
                        }

                        put {
                            // TODO: update stand data.
                        }
                    }

                    route("/volunteers") {
                        post {
                            // TODO: create a volunteer.
                        }

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
}

/**
 * Sends a structured JSON error response.
 *
 * @param message human-readable error message
 * @param code application-specific error code
 * @param status HTTP status to return
 */
suspend fun RoutingContext.sendError(message: String, code: Int, status: HttpStatusCode) {
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
 * Ensures the volunteer does not have any of the roles listed in [allowedRoles].
 *
 * If the volunteer has one of the forbidden roles, a `403 Forbidden` response is sent.
 *
 * @param volunteerId volunteer id to check
 * @param allowedRoles roles that are not allowed to perform the action
 * @return `true` if the request may continue, otherwise `false`
 */
private suspend fun RoutingContext.requireVolunteerWithoutRolesOrSendError(
    volunteerId: Int,
    vararg allowedRoles: RoleName
): Boolean {
    if (VolunteerValidation(VolunteerRepository()).volunteerHasRole(volunteerId, *allowedRoles)) {
        sendError(
            status = HttpStatusCode.Forbidden,
            message = "You are not allowed to perform this action",
            code = 403
        )
        return false
    }

    return true
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
    val principal = call.principal<JWTPrincipal>() ?: return null
    val volunteerId = principal.payload.subject.toInt()
    val volunteerRole = volunteerRepository.getRole(volunteerId)
    if (volunteerRole == null) {
        call.respond(
            status = HttpStatusCode.Unauthorized,
            message = ErrorMessage(
                message = "Unable to find the volunteer role",
                code = 401
            )
        )
        return null
    }
    return volunteerRole
}

/**
 * Dispatches [CardRepository.connect] to the correct overload based on runtime argument types.
 *
 * Both identifiers may be either an [Int] or a [String], which results in four valid combinations.
 *
 * @param card card identifier, either id or NFC code
 * @param user user identifier, either id or username
 */
private fun CardRepository.connect(card: Any, user: Any) = when {
    card is Int && user is Int -> connect(card, user)
    card is Int && user is String -> connect(card, user)
    card is String && user is Int -> connect(card, user)
    card is String && user is String -> connect(card, user)
    else -> error("Unexpected identifier types: ${card::class} / ${user::class}")
}