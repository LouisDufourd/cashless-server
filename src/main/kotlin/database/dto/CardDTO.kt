package fr.plaglefleau.database.dto

data class CardDTO(
    val id: Int,
    val nfc: String,
    val pin: Int,
    val balance: Double,
    val userId: Int?
)
