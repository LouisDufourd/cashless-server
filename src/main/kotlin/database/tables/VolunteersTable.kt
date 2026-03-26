package fr.plaglefleau.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object VolunteersTable : IntIdTable("volunteers") {
    // Login identifier for volunteer authentication.
    val username = varchar("username", 45).uniqueIndex()

    // Hashed password for volunteer login.
    val password = varchar("password", 255)

    // Foreign key to the role table.
    // Each volunteer must belong to one role.
    val roleId = reference("fk_role_id", RolesTable.id)

    // Foreign key to the civility table.
    // Each volunteer is linked to one civility record.
    val civilityId = reference("fk_civility_id", CivilitiesTable.id)

    // Foreign key to the stand table.
    // If your schema allows null here, mark it nullable in Exposed too.
    val standId = reference("fk_stand_id", StandsTable.id)
}