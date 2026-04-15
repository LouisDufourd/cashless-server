package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.TransactionLogDTO

interface ITransactionLogRepository {
    fun getCardTransactionLog(cardId: Int, page: Int?, pageSize: Int?): List<TransactionLogDTO>
    fun getCardTransactionLog(nfc: String, page: Int?, pageSize: Int?): List<TransactionLogDTO>
}