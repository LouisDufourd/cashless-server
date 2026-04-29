package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.dto.StandDTO
import fr.plaglefleau.database.entities.StandEntity
import fr.plaglefleau.database.exceptions.NotFoundException
import fr.plaglefleau.database.repositories.IStandRepository
import fr.plaglefleau.database.tables.StandsTable
import org.jetbrains.exposed.v1.core.eq

class StandRepository: IStandRepository {
    override fun getStand(identifier: Int) : StandDTO = dbQuery {
        val entity = StandEntity.findById(identifier) ?: throw NotFoundException("Stand with id $identifier not found")
        StandDTO(
            id = entity.id.value,
            entity.name,
            inventory = emptyList()
        )
    }

    override fun getStand(identifier: String) : StandDTO = dbQuery {
        val entity = StandEntity.find(StandsTable.name eq identifier).firstOrNull() ?: throw NotFoundException("Stand with name $identifier not found")
        StandDTO(
            id = entity.id.value,
            entity.name,
            inventory = emptyList()
        )
    }
}