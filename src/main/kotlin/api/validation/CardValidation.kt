package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.ICardRepository

class CardValidation(private val cardRepository: ICardRepository) {
    fun verifyInvalidAmount(amount: Double) : Boolean {
        return  amount < 0
    }

    fun cardExist(identifier: Any): Boolean {
        return when(identifier) {
            is Int -> cardRepository.getCard(identifier) != null
            is String -> cardRepository.getCard(identifier) != null
            else -> false
        }
    }

    fun canDebitCard(identifier: Any, amount: Double): Boolean {
        return when(identifier) {
            is Int -> (cardRepository.getBalance(identifier)?: -1.0) > amount
            is String -> (cardRepository.getBalance(identifier)?: -1.0) > amount
            else -> false
        }
    }
}