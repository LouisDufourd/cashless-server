package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.CardEntity
import fr.plaglefleau.database.entities.StandEntity
import fr.plaglefleau.database.entities.TransactionLogEntity
import fr.plaglefleau.database.entities.UserEntity
import fr.plaglefleau.database.tables.CardsTable
import fr.plaglefleau.database.tables.StandsTable
import fr.plaglefleau.database.tables.StandsTable.name
import fr.plaglefleau.database.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import java.math.BigDecimal
import kotlin.plus

class CardRepository {
    fun getBalance(id: Int): Double? = dbQuery {
        CardEntity.findById(id)?.balance?.toDouble()
    }

    fun getBalance(nfc: String): Double? = dbQuery {
        CardEntity.find(CardsTable.nfc eq nfc).firstOrNull()?.balance?.toDouble()
    }

    fun debit(identifier: Int, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance -= BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    fun debit(identifier: String, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance -= BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    fun connect(cardId: Int, userId: Int) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardId)!!
        cardEntity.userId = userRepository.getUser(userId)!!.id
    }

    fun connect(cardId: Int, username: String) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardId)!!
        cardEntity.userId = userRepository.getUser(username)!!.id
    }

    fun connect(nfcCode: String, userId: Int) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(nfcCode)!!
        cardEntity.userId = userRepository.getUser(userId)!!.id
    }

    fun connect(nfcCode: String, username: String) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(nfcCode)!!
        cardEntity.userId = userRepository.getUser(username)!!.id
    }

    fun getCardEntity(id: Int): CardEntity? = dbQuery {
        CardEntity.findById(id)
    }

    fun getCardEntity(nfc: String): CardEntity? = dbQuery {
        CardEntity.find(CardsTable.nfc eq nfc).firstOrNull()
    }

    fun credit(identifier: Int, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance += BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    fun credit(identifier: String, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance += BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    fun delete(identifier: Int) {
        CardEntity.findById(identifier)!!.delete()
    }

    fun delete(identifier: String) {
        CardEntity.find(CardsTable.nfc eq identifier).first().delete()
    }

    private fun logTransaction(card: CardEntity, amount: Double, standName: String) {
        TransactionLogEntity.new {
            this.amount = BigDecimal(amount)
            this.standId = StandEntity.find { name eq standName }.first().id
            this.cardId = card.id
            this.userId = card.userId
        }
    }
}