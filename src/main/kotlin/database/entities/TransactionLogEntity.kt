package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.TransactionLogsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class TransactionLogEntity(id: EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the transaction_logs table.
    companion object : IntEntityClass<TransactionLogEntity>(TransactionLogsTable)

    // Timestamp of the transaction.
    var date by TransactionLogsTable.date

    // Monetary amount of the transaction.
    var amount by TransactionLogsTable.amount

    // User reference.
    var userId by TransactionLogsTable.userId

    // Card reference.
    var cardId by TransactionLogsTable.cardId

    // Stand reference.
    var standId by TransactionLogsTable.standId
}