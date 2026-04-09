package fr.plaglefleau.database.repositories

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.VolunteerEntity
import fr.plaglefleau.database.tables.RoleName
import fr.plaglefleau.database.tables.VolunteersTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

class VolunteerRepository {
    fun login(username: String, password: String) : Int? {
        // Search for a volunteer with matching credentials.
        // The firstOrNull() call prevents crashes if no row exists.
        return dbQuery {
            VolunteerEntity.find {
                VolunteersTable.username eq username
                VolunteersTable.password eq password
            }.firstOrNull()?.id?.value
        }
    }

    fun getRole(volunteerId: Int): RoleName? = dbQuery {
        VolunteersTable
            .selectAll()
            .where { VolunteersTable.id eq volunteerId }
            .firstOrNull()
            ?.get(VolunteersTable.role)
    }
}