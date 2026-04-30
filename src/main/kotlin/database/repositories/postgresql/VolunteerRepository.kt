package fr.plaglefleau.database.repositories.postgresql

import fr.plaglefleau.database.DatabaseFactory.dbQuery
import fr.plaglefleau.database.entities.VolunteerEntity
import fr.plaglefleau.database.exceptions.AuthenticationException
import fr.plaglefleau.database.repositories.IVolunteerRepository
import fr.plaglefleau.database.tables.RoleName
import fr.plaglefleau.database.tables.VolunteersTable
import fr.plaglefleau.database.exceptions.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll

class VolunteerRepository: IVolunteerRepository {
    override fun login(username: String, password: String) : Int = dbQuery {
        // Search for a volunteer with matching credentials.
        // The firstOrNull() call prevents crashes if no row exists.
            val volunteer = VolunteerEntity.find {
                (VolunteersTable.username eq username) and (VolunteersTable.password eq password)
            }.firstOrNull()
            volunteer?.id?.value ?: throw AuthenticationException("Invalid credentials: $username / $password")
        }

    override fun getRole(volunteerId: Int): RoleName = dbQuery {
        VolunteersTable
            .selectAll()
            .where { VolunteersTable.id eq volunteerId }
            .firstOrNull()
            ?.get(VolunteersTable.role) ?: throw NotFoundException("Volunteer with id $volunteerId not found")
    }
}