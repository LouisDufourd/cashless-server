package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.dto.StandDTO
import fr.plaglefleau.database.entities.StandEntity
import fr.plaglefleau.database.tables.StandsTable
import org.jetbrains.exposed.v1.core.eq

class StandRepository {
    fun getStand(identifier: Int) : StandDTO? = dbQuery() {
        val entity = StandEntity.findById(identifier) ?: return@dbQuery null
        StandDTO(
            id = entity.id.value,
            entity.name,
            inventory = emptyList()
        )
    }

    fun getStand(identifier: String) : StandDTO? = dbQuery() {
        val entity = StandEntity.find(StandsTable.name eq identifier).firstOrNull() ?: return@dbQuery null
        StandDTO(
            id = entity.id.value,
            entity.name,
            inventory = emptyList()
        )
    }
}