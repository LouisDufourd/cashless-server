package fr.plaglefleau.database.dto

data class TransactionLogDTO(
    val id: Int,
    val date: String,
    val amount: Double,
    val userDTO: UserDTO,
    val cardDTO: CardDTO,
    val standDTO: StandDTO
)
