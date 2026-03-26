package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.CivilitiesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class CivilityEntity(id: EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the civilities table.
    companion object : IntEntityClass<CivilityEntity>(CivilitiesTable)

    // Last name column mapping.
    var lastname by CivilitiesTable.lastname

    // First name column mapping.
    var firstname by CivilitiesTable.firstname

    // Age column mapping.
    var age by CivilitiesTable.age
}