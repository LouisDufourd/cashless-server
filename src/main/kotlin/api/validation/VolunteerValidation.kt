package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.IVolunteerRepository
import fr.plaglefleau.database.tables.RoleName

class VolunteerValidation(private val volunteerRepository: IVolunteerRepository) {
    fun volunteerHasRole(volunteerId: Int, vararg roleNames: RoleName) : Boolean {
        val role = volunteerRepository.getRole(volunteerId) ?: return false
        return roleNames.contains(role)
    }
}