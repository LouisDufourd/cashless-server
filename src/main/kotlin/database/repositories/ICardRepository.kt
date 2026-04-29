package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.CardDTO
import fr.plaglefleau.database.exceptions.*

/**
 * Defines persistence operations for card data and card balance management.
 *
 * Implementations provide methods to retrieve cards, read balances, update card
 * information, perform credit and debit operations, and connect cards to users.
 */
interface ICardRepository {
    /**
     * Retrieves the current balance of a card by its internal identifier.
     *
     * @param identifier the internal card id
     * @return the current card balance
     * @throws NotFoundException if no card exists with this id
     */
    fun getBalance(identifier: Int): Double

    /**
     * Retrieves the current balance of a card by its NFC code.
     *
     * @param identifier the card NFC code
     * @return the current card balance
     * @throws NotFoundException if no card exists with this NFC code
     */
    fun getBalance(identifier: String): Double

    /**
     * Debits an amount from a card identified by its internal identifier.
     *
     * @param identifier the internal card id
     * @param amount the amount to debit
     * @param standName the stand associated with the transaction
     * @throws NotFoundException if no card exists with this id or if the stand is not found
     * @throws InsufficientFundsException if the card balance is insufficient
     */
    fun debit(identifier: Int, amount: Double, standName: String)

    /**
     * Debits an amount from a card identified by its NFC code.
     *
     * @param identifier the card NFC code
     * @param amount the amount to debit
     * @param standName the stand associated with the transaction
     * @throws NotFoundException if no card exists with this id or if the stand is not found
     * @throws InsufficientFundsException if the card balance is insufficient
     */
    fun debit(identifier: String, amount: Double, standName: String)

    /**
     * Credits an amount to a card identified by its internal identifier.
     *
     * @param identifier the internal card id
     * @param amount the amount to credit
     * @throws NotFoundException if no card exists with this id
     */
    fun credit(identifier: Int, amount: Double)

    /**
     * Credits an amount to a card identified by its NFC code.
     *
     * @param identifier the card NFC code
     * @param amount the amount to credit
     * @throws NotFoundException if no card exists with this NFC code
     */
    fun credit(identifier: String, amount: Double)

    /**
     * Connects a card to a user using their internal identifiers.
     *
     * @param cardIdentifier the internal card id
     * @param userId the internal user id
     * @throws NotFoundException if no card exists with this id
     * @throws NotFoundException if no user exists with this id
     */
    fun connect(cardIdentifier: Int, userId: Int)

    /**
     * Connects a card to a user by card id and username.
     *
     * @param cardIdentifier the internal card id
     * @param username the username of the user to connect
     * @throws NotFoundException if no card exists with this id
     * @throws NotFoundException if no user exists with this username
     */
    fun connect(cardIdentifier: Int, username: String)

    /**
     * Connects a card to a user by NFC code and user id.
     *
     * @param cardIdentifier the card NFC code
     * @param userIdentifier the internal user id
     * @throws NotFoundException if no card exists with this NFC code
     * @throws NotFoundException if no user exists with this id
     */
    fun connect(cardIdentifier: String, userIdentifier: Int)

    /**
     * Connects a card to a user using the card NFC code and username.
     *
     * @param cardIdentifier the card NFC code
     * @param userIdentifier the username of the user to connect
     * @throws NotFoundException if no card exists with this NFC code
     * @throws NotFoundException if no user exists with this username
     */
    fun connect(cardIdentifier: String, userIdentifier: String)

    /**
     * Creates a new card.
     *
     * @param pin the card PIN code (1000-9999)
     * @param identifier the unique NFC code associated with the card
     * @return the created card entity
     * @throws ConflictException if the NFC code is not unique
     */
    fun create(pin: Int, identifier: String): CardDTO

    /**
     * Updates a card identified by its internal identifier.
     *
     * The implementation should apply only non-null values.
     *
     * @param identifier the internal card identifier
     * @param pin the new PIN code, or `null` to keep the current one
     * @param amount the new card balance, or `null` to keep the current one
     * @throws NotFoundException if no card exists with this identifier
     */
    fun update(identifier: Int, pin: Int?, amount: Double?)

    /**
     * Updates a card identified by its NFC code.
     *
     * The implementation should apply only non-null values.
     *
     * @param identifier the card NFC code
     * @param pin the new PIN code, or `null` to keep the current one
     * @param amount the new card balance, or `null` to keep the current one
     * @throws NotFoundException if no card exists with this NFC code
     */
    fun update(identifier: String, pin: Int?, amount: Double?)

    /**
     * Retrieves a card by its internal identifier.
     *
     * @param identifier the internal card id
     * @return the matching card data
     * @throws NotFoundException if no card exists with this id
     */
    fun getCard(identifier: Int): CardDTO

    /**
     * Retrieves a card by its NFC code.
     *
     * @param identifier the card NFC code
     * @return the matching card data
     * @throws NotFoundException if no card exists with this NFC code
     */
    fun getCard(identifier: String): CardDTO
}
