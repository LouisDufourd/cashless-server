package fr.plaglefleau.database.dto

data class UserDTO(
    val id: Int,
    val username: String,
    val password: String,
    val cards: List<CardDTO>
)