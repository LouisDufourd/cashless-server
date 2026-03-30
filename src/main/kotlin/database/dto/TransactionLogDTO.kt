package fr.plaglefleau.database.dto

import kotlin.time.Instant

data class TransactionLogDTO(
    val id: Int,
    val date: Instant,
    val amount: Double,
    val userDTO: UserDTO?,
    val cardDTO: CardDTO,
    val standDTO: StandDTO
)
