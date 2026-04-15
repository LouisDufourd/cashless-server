package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.ICardRepository

/**
 * Provides validation helpers for card-related business rules.
 *
 * This class centralizes checks that are reused by routes and services so that
 * card existence, balance, and amount validation stay consistent across the application.
 */
class CardValidation(private val cardRepository: ICardRepository) {

    /**
     * Returns `true` when the provided amount is invalid for a payment operation.
     *
     * @param amount the amount to validate
     * @return `true` if the amount is negative, otherwise `false`
     */
    fun verifyInvalidAmount(amount: Double) : Boolean {
        return amount < 0
    }

    /**
     * Checks whether a card exists for the given identifier.
     *
     * The identifier can be either an internal numeric id or an NFC string.
     *
     * @param identifier the card identifier to check
     * @return `true` if a matching card exists, otherwise `false`
     */
    fun cardExist(identifier: Any): Boolean {
        return when(identifier) {
            is Int -> cardRepository.getCard(identifier) != null
            is String -> cardRepository.getCard(identifier) != null
            else -> false
        }
    }

    /**
     * Checks whether a card has enough balance to allow a debit.
     *
     * The identifier can be either an internal numeric id or an NFC string.
     *
     * @param identifier the card identifier to check
     * @param amount the amount that should be debited
     * @return `true` if the card balance is strictly greater than the amount, otherwise `false`
     */
    fun canDebitCard(identifier: Any, amount: Double): Boolean {
        return when(identifier) {
            is Int -> (cardRepository.getBalance(identifier) ?: -1.0) > amount
            is String -> (cardRepository.getBalance(identifier) ?: -1.0) > amount
            else -> false
        }
    }
}