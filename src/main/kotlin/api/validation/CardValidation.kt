package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.CardRepository

object CardValidation {
    fun verifyInvalidAmount(amount: Double) : Boolean {
        return  amount < 0
    }

    fun cardExist(identifier: Any): Boolean {
        return when(identifier) {
            is Int -> CardRepository().getCard(identifier) != null
            is String -> CardRepository().getCard(identifier) != null
            else -> false
        }
    }

    fun canDebitCard(identifier: Any, amount: Double): Boolean {
        return when(identifier) {
            is Int -> (CardRepository().getBalance(identifier)?: -1.0) > amount
            is String -> (CardRepository().getBalance(identifier)?: -1.0) > amount
            else -> false
        }
    }
}