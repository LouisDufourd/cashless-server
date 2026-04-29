package fr.plaglefleau.database.exceptions

class AuthenticationException(message: String): Exception(message)
class ConflictException(message: String): Exception(message)
class ForbiddenException(message: String): Exception(message)
class NotFoundException(message: String): Exception(message)
class InsufficientFundsException(message: String): Exception(message)
