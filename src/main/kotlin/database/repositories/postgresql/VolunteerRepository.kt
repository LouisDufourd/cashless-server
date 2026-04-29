package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.VolunteerEntity
import fr.plaglefleau.database.repositories.IVolunteerRepository
import fr.plaglefleau.database.tables.RoleName
import fr.plaglefleau.database.tables.VolunteersTable
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.jvm.Throws

class VolunteerRepository: IVolunteerRepository {
    override fun login(username: String, password: String) : Int {
        // Search for a volunteer with matching credentials.
        // The firstOrNull() call prevents crashes if no row exists.
        return dbQuery {
            VolunteerEntity.find {
                VolunteersTable.username eq username
                VolunteersTable.password eq password
            }.firstOrNull()?.id?.value ?: throw NotFoundException("Volunteer with username $username not found")
        }
    }

    override fun getRole(volunteerId: Int): RoleName = dbQuery {
        VolunteersTable
            .selectAll()
            .where { VolunteersTable.id eq volunteerId }
            .firstOrNull()
            ?.get(VolunteersTable.role) ?: throw NotFoundException("Volunteer with id $volunteerId not found")
    }
}