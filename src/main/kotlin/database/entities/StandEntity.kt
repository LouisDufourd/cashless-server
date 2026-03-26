package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.StandsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class StandEntity(id : EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the stands table.
    companion object : IntEntityClass<StandEntity>(StandsTable)

    // Stand name value.
    var name by StandsTable.name
}