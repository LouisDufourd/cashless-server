package fr.plaglefleau.database.entities

import fr.plaglefleau.database.tables.VolunteersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class VolunteerEntity(id : EntityID<Int>) : IntEntity(id) {
    // Connect this entity class to the volunteers table.
    companion object : IntEntityClass<VolunteerEntity>(VolunteersTable)

    // Login username.
    var username by VolunteersTable.username

    // Login password.
    var password by VolunteersTable.password

    var role by VolunteersTable.role

    // Civility association.
    var civilityId by VolunteersTable.civilityId

    // Optional stand association.
    var standId by VolunteersTable.standId
}