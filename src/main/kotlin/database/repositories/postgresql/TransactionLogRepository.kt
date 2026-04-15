package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.dto.TransactionLogDTO
import fr.plaglefleau.database.repositories.ITransactionLogRepository
import fr.plaglefleau.database.tables.CardsTable
import fr.plaglefleau.database.tables.StandsTable
import fr.plaglefleau.database.tables.TransactionLogsTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

class TransactionLogRepository : ITransactionLogRepository {
    override fun getCardTransactionLog(cardId: Int, page: Int?, pageSize: Int?): List<TransactionLogDTO> = dbQuery {
        TransactionLogsTable
            .join(
                otherTable = CardsTable,
                JoinType.INNER,
                onColumn = TransactionLogsTable.cardId,
                otherColumn = CardsTable.id
            )
            .join(
                otherTable = StandsTable,
                JoinType.INNER,
                onColumn = TransactionLogsTable.standId,
                otherColumn = StandsTable.id
            )
            .selectAll()
            .where { TransactionLogsTable.cardId eq cardId }
            .limit(count = pageSize?.coerceAtLeast(minimumValue = 1) ?: 10)
            .offset(start = calculateOffset(page, pageSize))
            .orderBy(column = TransactionLogsTable.date, order = SortOrder.DESC)
            .map { row ->
                TransactionLogDTO(
                    id = row[TransactionLogsTable.id].value,
                    date = row[TransactionLogsTable.date].toEpochMilliseconds(),
                    amount = row[TransactionLogsTable.amount].toDouble(),
                    cardNFC = row[CardsTable.nfc],
                    standName = row[StandsTable.name]
                )
            }
    }

    override fun getCardTransactionLog(nfc: String, page: Int?, pageSize: Int?): List<TransactionLogDTO> = dbQuery {
        TransactionLogsTable
            .join(
                otherTable = CardsTable,
                JoinType.INNER,
                onColumn = TransactionLogsTable.cardId,
                otherColumn = CardsTable.id
            )
            .join(
                otherTable = StandsTable,
                JoinType.INNER,
                onColumn = TransactionLogsTable.standId,
                otherColumn = StandsTable.id
            )
            .selectAll()
            .where { CardsTable.nfc eq nfc }
            .limit(count = pageSize?.coerceAtLeast(minimumValue = 1) ?: 10)
            .offset(start = calculateOffset(page, pageSize))
            .orderBy(column = TransactionLogsTable.date, order = SortOrder.DESC)
            .map { row ->
                TransactionLogDTO(
                    id = row[TransactionLogsTable.id].value,
                    date = row[TransactionLogsTable.date].toEpochMilliseconds(),
                    amount = row[TransactionLogsTable.amount].toDouble(),
                    cardNFC = row[CardsTable.nfc],
                    standName = row[StandsTable.name]
                )
            }
    }

    private fun calculateOffset(page: Int?, pageSize: Int?): Long {
        val pageNumber = page?.coerceAtLeast(minimumValue = 1) ?: 1
        val pageSizeNumber = pageSize?.coerceAtLeast(minimumValue = 1) ?: 10

        val offset = pageNumber.minus(1).times(pageSizeNumber)
        return offset.toLong()
    }
}