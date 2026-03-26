package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.TransactionLogEntity
import fr.plaglefleau.database.tables.TransactionLogsTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

class TransactionLogRepository {
    fun getCardTransactionLog(cardId: Int, page: Int = 1) : List<TransactionLogEntity> = dbQuery {
        val pageSize = 10

        //TODO : Select transaction log for a card with pagination
    }
}