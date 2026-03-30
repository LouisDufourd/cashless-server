package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.dto.CardDTO
import fr.plaglefleau.database.dto.StandDTO
import fr.plaglefleau.database.dto.TransactionLogDTO
import fr.plaglefleau.database.dto.UserDTO
import fr.plaglefleau.database.tables.CardsTable
import fr.plaglefleau.database.tables.StandsTable
import fr.plaglefleau.database.tables.TransactionLogsTable
import fr.plaglefleau.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

class TransactionLogRepository {
    fun getCardTransactionLog(cardId: Int, page: Int?, pageSize: Int?): List<TransactionLogDTO> = dbQuery {
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
            .join(
                otherTable = UsersTable,
                JoinType.LEFT,
                onColumn = TransactionLogsTable.userId,
                otherColumn = UsersTable.id
            )
            .selectAll()
            .where { TransactionLogsTable.cardId eq cardId }
            .limit(pageSize?.coerceAtLeast(minimumValue = 1)?: 10)
            .offset(calculateOffset(page, pageSize))
            .map { row ->
                val userDTO = row.getOrNull(UsersTable.id)?.let { userId ->
                    UserDTO(
                        id = userId.value,
                        username = row[UsersTable.username],
                        password = "",
                        cards = emptyList()
                    )
                }

                TransactionLogDTO(
                    id = row[TransactionLogsTable.id].value,
                    date = row[TransactionLogsTable.date],
                    amount = row[TransactionLogsTable.amount].toDouble(),
                    userDTO = userDTO,
                    cardDTO = CardDTO(
                        id = row[CardsTable.id],
                        pin = row[CardsTable.pin],
                        balance = row[CardsTable.balance],
                        nfc = row[CardsTable.nfc]
                    ),
                    standDTO = StandDTO(
                        id = row[StandsTable.id].value,
                        name = row[StandsTable.name],
                        inventory = emptyList()
                    ),
                )
            }
    }

    private fun calculateOffset(page: Int?, pageSize: Int?) : Long {
        val pageNumber = page?.coerceAtLeast(minimumValue = 1) ?: 1
        val pageSizeNumber = pageSize?.coerceAtLeast(minimumValue = 1)?: 10

        val offset = pageNumber.minus(1).times(pageSizeNumber)
        return offset.toLong()
    }
}