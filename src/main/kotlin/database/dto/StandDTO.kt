package fr.plaglefleau.database.dto

data class StandDTO(
    val id: Int,
    val name: String,
    val inventory: List<InventoryDTO>
)