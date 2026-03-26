package fr.plaglefleau.database.dto

data class CardDTO(
    val id: Int,
    val pin: Int,
    val balance: Double,
    val nfc: String
)
