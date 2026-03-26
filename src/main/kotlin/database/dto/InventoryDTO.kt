package fr.plaglefleau.database.dto

data class InventoryDTO(
    val id: Int,
    val quantity: Int,
    val price: Double,
    val article: ArticleDTO
)