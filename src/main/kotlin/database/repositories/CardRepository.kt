package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.CardEntity
import fr.plaglefleau.database.tables.CardsTable
import org.jetbrains.exposed.v1.core.eq

class CardRepository {
    fun getBalance(id: Int): Double? = dbQuery {
        CardEntity.findById(id)?.balance?.toDouble()
    }

    fun getBalance(nfc: String): Double? = dbQuery {
        CardEntity.find(CardsTable.nfc eq nfc).firstOrNull()?.balance?.toDouble()
    }
}