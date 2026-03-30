package fr.plaglefleau.database.dto

import kotlin.time.Instant

data class TransactionLogDTO(
    val id: Int,
    val date: Long,
    val amount: Double,
    val cardNFC: String,
    val standName: String
)
