package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import java.math.BigDecimal

object InventoryTable : IntIdTable("inventory") {
    // Number of items available for this article/stand combination.
    val quantity = integer("quantity").default(0)

    // Price stored with two decimal places.
    val price = decimal("price", 10, 2).default(BigDecimal(0))

    // Reference to the article being stored in inventory.
    val articleId = reference("fk_article_id", ArticlesTable.id)

    // Reference to the stand where this inventory item belongs.
    val standId = reference("fk_stand_id", StandsTable.id)
}