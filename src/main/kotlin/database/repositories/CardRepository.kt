package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.CardEntity
import fr.plaglefleau.database.entities.StandEntity
import fr.plaglefleau.database.entities.TransactionLogEntity
import fr.plaglefleau.database.tables.CardsTable
import fr.plaglefleau.database.tables.StandsTable
import fr.plaglefleau.database.tables.StandsTable.name
import org.jetbrains.exposed.v1.core.eq
import java.math.BigDecimal

class CardRepository {
    fun getBalance(id: Int): Double? = dbQuery {
        CardEntity.findById(id)?.balance?.toDouble()
    }

    fun getBalance(nfc: String): Double? = dbQuery {
        CardEntity.find(CardsTable.nfc eq nfc).firstOrNull()?.balance?.toDouble()
    }

    fun debit(id: Int, amount: Double, standName: String) = dbQuery {
        CardEntity.findByIdAndUpdate(id) {
            it.balance -= BigDecimal(amount)
        }

        TransactionLogEntity.new {
            val card = CardEntity.findById(id)!!
            this.amount = BigDecimal(amount)
            this.standId = StandEntity.find { name eq standName }.first().id
            this.cardId = card.id
            this.userId = card.userId
        }
    }

    fun debit(nfcCode: String, amount: Double, standName: String) = dbQuery {
        CardEntity.findSingleByAndUpdate(CardsTable.nfc eq nfcCode) {
            it.balance -= BigDecimal(amount)
        }

        TransactionLogEntity.new {
            val card = CardEntity.find(CardsTable.nfc eq nfcCode).first()
            this.amount = BigDecimal(amount)
            this.standId = StandEntity.find { name eq standName }.first().id
            this.cardId = card.id
            this.userId = card.userId
        }
    }
}