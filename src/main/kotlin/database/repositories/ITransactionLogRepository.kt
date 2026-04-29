package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.TransactionLogDTO

/**
 * Defines persistence operations for reading card transaction history.
 *
 * Implementations provide paginated access to transaction logs for a card,
 * identified either by its internal numeric id or by its NFC code.
 */
interface ITransactionLogRepository {

    /**
     * Retrieves the transaction history for a card by its internal identifier.
     *
     * Results are paginated using [page] and [pageSize].
     *
     * @param volunteerIdentifier the internal card id
     * @param page the page number to retrieve, starting at `1`
     * @param pageSize the maximum number of transactions to return
     * @return a list of transaction log entries for the requested page
     */
    fun getCardTransactionLog(volunteerIdentifier: Int, page: Int = 1, pageSize: Int = 10): List<TransactionLogDTO>

    /**
     * Retrieves the transaction history for a card by its NFC code.
     *
     * Results are paginated using [page] and [pageSize].
     *
     * @param volunteerIdentifier the card NFC code
     * @param page the page number to retrieve, starting at `1`
     * @param pageSize the maximum number of transactions to return
     * @return a list of transaction log entries for the requested page
     */
    fun getCardTransactionLog(volunteerIdentifier: String, page: Int = 1, pageSize: Int = 10): List<TransactionLogDTO>
}