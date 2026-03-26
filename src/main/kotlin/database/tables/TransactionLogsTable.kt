package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object TransactionLogsTable : IntIdTable("transaction_logs") {
    // Timestamp of when the transaction occurred.
    // Defaults to the current system time when the row is created.
    val date = timestamp("date").default(Clock.System.now())

    // Transaction amount, stored as a decimal value.
    val amount = decimal("amount", 10, 2)

    // User involved in the transaction.
    val userId = reference("fk_user_id", UsersTable.id)

    // Card used in the transaction.
    val cardId = reference("fk_card_id", CardsTable.id)

    // Stand where the transaction happened.
    val standId = reference("fk_stand_id", StandsTable.id)
}