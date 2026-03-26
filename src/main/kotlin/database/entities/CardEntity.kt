package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.CardsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class CardEntity(id: EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the cards table.
    companion object : IntEntityClass<CardEntity>(CardsTable)

    // Card PIN.
    var pin by CardsTable.pin

    // Current balance of the card.
    var balance by CardsTable.balance

    // NFC identifier.
    var nfc by CardsTable.nfc

    // Foreign key to the owning user.
    var userId by CardsTable.userId
}