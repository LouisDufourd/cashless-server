package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.ICardRepository

/**
 * Provides validation helpers for card-related business rules.
 *
 * This class centralizes checks that are reused by routes and services so that
 * card existence, balance, and amount validation stay consistent across the application.
 */
object CardValidation {

    /**
     * Returns `true` when the provided amount is invalid for a payment operation.
     *
     * @param amount the amount to validate
     * @return `true` if the amount is negative, otherwise `false`
     */
    fun verifyInvalidAmount(amount: Double) : Boolean {
        return amount < 0
    }
}