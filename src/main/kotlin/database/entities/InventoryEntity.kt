package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.InventoryTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class InventoryEntity(id: EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the inventory table.
    companion object : IntEntityClass<InventoryEntity>(InventoryTable)

    // Quantity stored for this inventory row.
    var quantity by InventoryTable.quantity

    // Price for the inventory item.
    var price by InventoryTable.price

    // Related article ID.
    var articleId by InventoryTable.articleId

    // Related stand ID.
    var standId by InventoryTable.standId
}