package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.dto.CardDTO
import fr.plaglefleau.database.entities.CardEntity
import fr.plaglefleau.database.entities.StandEntity
import fr.plaglefleau.database.entities.TransactionLogEntity
import fr.plaglefleau.database.repositories.ICardRepository
import fr.plaglefleau.database.tables.CardsTable
import fr.plaglefleau.database.tables.StandsTable.name
import org.jetbrains.exposed.v1.core.eq
import java.math.BigDecimal

/**
 * Handles all database operations related to NFC cards.
 *
 * Cards can be identified either by their internal integer ID or by their NFC code string.
 * Most operations are therefore overloaded to accept both identifier types.
 *
 * All database access goes through [dbQuery], which wraps operations in an Exposed transaction.
 */
class CardRepository: ICardRepository {

    /**
     * Returns the current balance of the card identified by its internal database [id],
     * or null if no card with that ID exists.
     */
    override fun getBalance(id: Int): Double? = dbQuery {
        CardEntity.findById(id)?.balance?.toDouble()
    }

    /**
     * Returns the current balance of the card identified by its [nfc] code,
     * or null if no card with that NFC code exists.
     */
    override fun getBalance(nfc: String): Double? = dbQuery {
        CardEntity.find(CardsTable.nfc eq nfc).firstOrNull()?.balance?.toDouble()
    }

    /**
     * Subtracts [amount] from the balance of the card with the given internal [identifier],
     * then records a transaction log entry for the [standName] that performed the debit.
     *
     * Throws if no card is found (!! operator).
     */
    override fun debit(identifier: Int, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance -= BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    /**
     * Subtracts [amount] from the balance of the card with the given NFC [identifier],
     * then records a transaction log entry for the [standName] that performed the debit.
     *
     * Throws if no card is found (!! operator).
     */
    override fun debit(identifier: String, amount: Double, standName: String) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance -= BigDecimal(amount)
        logTransaction(card, amount, standName)
    }

    /**
     * Associates the card identified by [cardId] with the user identified by [userId].
     * Looks up both entities and sets the card's userId foreign key.
     */
    override fun connect(cardId: Int, userId: Int) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardId)!!
        cardEntity.userId = userRepository.getUser(userId)!!.id
    }

    /**
     * Associates the card identified by [cardId] with the user identified by [username].
     */
    override fun connect(cardId: Int, username: String) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(cardId)!!
        cardEntity.userId = userRepository.getUser(username)!!.id
    }

    /**
     * Associates the card identified by its [nfcCode] with the user identified by [userId].
     */
    override fun connect(nfcCode: String, userId: Int) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(nfcCode)!!
        cardEntity.userId = userRepository.getUser(userId)!!.id
    }

    /**
     * Associates the card identified by its [nfcCode] with the user identified by [username].
     */
    override fun connect(nfcCode: String, username: String) = dbQuery {
        val userRepository = UserRepository()
        val cardEntity = getCardEntity(nfcCode)!!
        cardEntity.userId = userRepository.getUser(username)!!.id
    }

    /**
     * Looks up a [CardEntity] by its internal database [id].
     * Returns null if no card with that ID exists.
     */
    private fun getCardEntity(id: Int): CardEntity? = dbQuery {
        CardEntity.findById(id)
    }

    /**
     * Looks up a [CardEntity] by its [nfc] code.
     * Returns null if no card with that NFC code exists.
     */
    private fun getCardEntity(nfc: String): CardEntity? = dbQuery {
        CardEntity.find(CardsTable.nfc eq nfc).firstOrNull()
    }

    /**
     * Adds [amount] to the balance of the card with the given internal [identifier].
     *
     * Throws if no card is found (!! operator).
     */
    override fun credit(identifier: Int, amount: Double) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance += BigDecimal(amount)
    }

    /**
     * Adds [amount] to the balance of the card with the given NFC [identifier].
     *
     * Throws if no card is found (!! operator).
     */
    override fun credit(identifier: String, amount: Double) = dbQuery {
        val card = getCardEntity(identifier)!!
        card.balance += BigDecimal(amount)
    }

    /**
     * Creates a new [TransactionLogEntity] recording that a debit of [amount] was made on [card]
     * at the stand named [standName].
     *
     * The stand is looked up by name; this will throw if no matching stand exists.
     */
    private fun logTransaction(card: CardEntity, amount: Double, standName: String) {
        TransactionLogEntity.new {
            this.amount = BigDecimal(amount)
            this.standId = StandEntity.find { name eq standName }.first().id
            this.cardId = card.id
            this.userId = card.userId
        }
    }

    /**
     * Creates a new card in the database with the given [pin] and [nfcCode].
     * The initial balance defaults to whatever value is set in the entity defaults.
     */
    override fun create(pin: Int, nfcCode: String) = dbQuery {
        CardEntity.new {
            this.nfc = nfcCode
            this.pin = pin
        }
    }

    /**
     * Updates the [pin] and/or [balance][amount] of the card identified by [identifier].
     * Fields with a null value are left unchanged.
     */
    override fun update(identifier: Int, pin: Int?, amount: Double?) = dbQuery {
        val card = CardEntity.findById(identifier)!!
        if (pin != null)
            card.pin = pin

        if (amount != null)
            card.balance = BigDecimal(amount)
    }

    /**
     * Updates the [pin] and/or [balance][amount] of the card identified by its [identifier].
     * Fields with a null value are left unchanged.
     */
    override fun update(identifier: String, pin: Int?, amount: Double?) = dbQuery {
        val card = CardEntity.find(CardsTable.nfc eq identifier).first()
        if (pin != null)
            card.pin = pin

        if (amount != null)
            card.balance = BigDecimal(amount)
    }

    override fun getCard(identifier: Int): CardDTO? = dbQuery {
        val entity = CardEntity.findById(identifier) ?: return@dbQuery null
        CardDTO(
            id = entity.id.value,
            nfcCode = entity.nfc,
            entity.pin,
            balance = entity.balance.toDouble(),
            userId = entity.userId?.value
        )
    }

    override fun getCard(identifier: String) = dbQuery {
        val entity = CardEntity.find { CardsTable.nfc eq identifier }.firstOrNull() ?: return@dbQuery null
        CardDTO(
            id = entity.id.value,
            nfcCode = entity.nfc,
            entity.pin,
            balance = entity.balance.toDouble(),
            userId = entity.userId?.value
        )
    }
}
