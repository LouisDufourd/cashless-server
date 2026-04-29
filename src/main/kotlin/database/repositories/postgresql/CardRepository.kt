package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.dto.CardDTO
import fr.plaglefleau.database.entities.CardEntity
import fr.plaglefleau.database.entities.StandEntity
import fr.plaglefleau.database.entities.TransactionLogEntity
import fr.plaglefleau.database.exceptions.InsufficientFundsException
import fr.plaglefleau.database.exceptions.NotFoundException
import fr.plaglefleau.database.repositories.ICardRepository
import fr.plaglefleau.database.tables.CardsTable
import fr.plaglefleau.database.tables.StandsTable.name
import org.jetbrains.exposed.v1.core.eq
import java.math.BigDecimal

class CardRepository : ICardRepository {

    override fun getBalance(identifier: Int): Double = dbQuery {
        getCardEntity(identifier).balance.toDouble()
    }

    override fun getBalance(identifier: String): Double = dbQuery {
        getCardEntity(identifier).balance.toDouble()
    }

    override fun debit(identifier: Int, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)
        if (card.balance < BigDecimal(amount)) throw InsufficientFundsException("Insufficient funds for debiting $amount from card $identifier")
        card.balance -= BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    override fun debit(identifier: String, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)
        if (card.balance < BigDecimal(amount)) throw InsufficientFundsException("Insufficient funds for debiting $amount from card $identifier")
        card.balance -= BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    override fun connect(cardIdentifier: Int, userId: Int) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardIdentifier)
        cardEntity.userId = userRepository.getUser(userId).id
    }

    override fun connect(cardIdentifier: Int, username: String) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardIdentifier)
        cardEntity.userId = userRepository.getUser(username).id
    }

    override fun connect(cardIdentifier: String, userIdentifier: Int) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardIdentifier)
        cardEntity.userId = userRepository.getUser(userIdentifier).id
    }

    override fun connect(cardIdentifier: String, userIdentifier: String) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardIdentifier)
        cardEntity.userId = userRepository.getUser(userIdentifier).id
    }


    override fun credit(identifier: Int, amount: Double) = dbQuery {
        val card = getCardEntity(identifier)
        card.balance += BigDecimal(amount)
    }

    override fun credit(identifier: String, amount: Double) = dbQuery {
        val card = getCardEntity(identifier)
        card.balance += BigDecimal(amount)
    }


    override fun create(pin: Int, identifier: String): CardDTO = dbQuery {
        val entity = CardEntity.new {
            this.nfc = identifier
            this.pin = pin
        }

        CardDTO(
            id = entity.id.value,
            nfc = entity.nfc,
            pin = entity.pin,
            balance = entity.balance.toDouble(),
            userId = entity.userId?.value
        )
    }

    override fun update(identifier: Int, pin: Int?, amount: Double?) = dbQuery {
        val card = getCardEntity(identifier)
        if (pin != null)
            card.pin = pin

        if (amount != null)
            card.balance = BigDecimal(amount)
    }


    override fun update(identifier: String, pin: Int?, amount: Double?) = dbQuery {
        val card = getCardEntity(identifier)
        if (pin != null)
            card.pin = pin

        if (amount != null)
            card.balance = BigDecimal(amount)
    }

    override fun getCard(identifier: Int): CardDTO = dbQuery {
        val entity = getCardEntity(identifier)
        CardDTO(
            id = entity.id.value,
            nfc = entity.nfc,
            pin = entity.pin,
            balance = entity.balance.toDouble(),
            userId = entity.userId?.value
        )
    }

    override fun getCard(identifier: String): CardDTO = dbQuery {
        val entity = getCardEntity(identifier)
        CardDTO(
            id = entity.id.value,
            nfc = entity.nfc,
            pin = entity.pin,
            balance = entity.balance.toDouble(),
            userId = entity.userId?.value
        )
    }

    private fun getCardEntity(id: Int): CardEntity = dbQuery {
        CardEntity.findById(id) ?: throw NotFoundException("Card with id $id not found")
    }

    private fun getCardEntity(nfc: String): CardEntity = dbQuery {
        CardEntity.find(CardsTable.nfc eq nfc).firstOrNull() ?: throw NotFoundException("Card with nfc $nfc not found")
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
