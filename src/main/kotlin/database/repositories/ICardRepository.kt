package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.dto.CardDTO
import fr.plaglefleau.database.entities.CardEntity

interface ICardRepository {
    fun getBalance(id: Int): Double?
    fun getBalance(nfc: String): Double?
    fun debit(identifier: Int, amount: Double, standName: String)
    fun debit(identifier: String, amount: Double, standName: String)
    fun credit(identifier: Int, amount: Double)
    fun credit(identifier: String, amount: Double)
    fun connect(cardId: Int, userId: Int)
    fun connect(cardId: Int, username: String)
    fun connect(nfcCode: String, userId: Int)
    fun connect(nfcCode: String, username: String)
    fun create(pin: Int, nfcCode: String): CardEntity
    fun update(identifier: Int, pin: Int?, amount: Double?)
    fun update(identifier: String, pin: Int?, amount: Double?)
    fun getCard(identifier: Int): CardDTO?
    fun getCard(identifier: String): CardDTO?
}
