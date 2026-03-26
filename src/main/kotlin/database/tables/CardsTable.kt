package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import java.math.BigDecimal

object CardsTable : IntIdTable("cards") {
    // PIN code used for card access.
    val pin = integer("pin")

    // Monetary balance stored with precision so currency values are safe.
    // Default value is zero for new cards.
    val balance = decimal("balance", 10, 2).default(BigDecimal(0))

    // NFC identifier of the card.
    // Unique because one NFC tag should map to only one card.
    val nfc = varchar("nfc", 45).uniqueIndex()

    // Foreign key to the owning user.
    // This column stores the user's ID and enforces referential integrity.
    val userId = reference("fk_user_id", UsersTable.id)
}