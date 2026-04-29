package fr.plaglefleau.api

import fr.plaglefleau.database.exceptions.*
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException

object ExceptionHandler {
    fun handleException(exception: Exception): HandleResponse {
        return when(exception) {
            is IllegalArgumentException -> HandleResponse(status = HttpStatusCode.BadRequest, message = "Bad request",  code = 400)
            is AuthenticationException -> HandleResponse(status = HttpStatusCode.Unauthorized, message = "Invalid credentials",  code = 401)
            is ForbiddenException -> HandleResponse(status = HttpStatusCode.Forbidden, message = "You are not allowed to access this resource",  code = 403)
            is NotFoundException -> HandleResponse(status = HttpStatusCode.NotFound, message = "We could not find the requested resource",  code = 404)
            is ConflictException -> HandleResponse(status = HttpStatusCode.Conflict, message = "The request conflicts with the current state of the resource",  code = 409)
            is InsufficientFundsException -> HandleResponse(status = HttpStatusCode.PaymentRequired, message = "Insufficient funds on the card",  code = 402)
            else -> HandleResponse(status = HttpStatusCode.InternalServerError, message = "Internal server error", code = 500)
        }
    }

    class HandleResponse(val status: HttpStatusCode, val message: String, val code: Int)
}