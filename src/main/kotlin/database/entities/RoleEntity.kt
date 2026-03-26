package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.RolesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class RoleEntity(id: EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the roles table.
    companion object : IntEntityClass<RoleEntity>(RolesTable)

    // Role label stored in the table.
    var name by RolesTable.name
}