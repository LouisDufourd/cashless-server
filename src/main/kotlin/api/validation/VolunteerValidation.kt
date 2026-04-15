package fr.plaglefleau.api.validation

import fr.plaglefleau.database.repositories.IVolunteerRepository
import fr.plaglefleau.database.tables.RoleName

/**
 * Provides validation helpers for volunteer-related business rules.
 *
 * This class centralizes role checks so authorization logic stays consistent
 * across routes and services.
 */
class VolunteerValidation(private val volunteerRepository: IVolunteerRepository) {

    /**
     * Checks whether the given volunteer has one of the specified roles.
     *
     * If the volunteer does not exist or its role cannot be read, this returns `false`.
     *
     * @param volunteerId the volunteer id to inspect
     * @param roleNames one or more roles that are allowed
     * @return `true` if the volunteer has one of the provided roles, otherwise `false`
     */
    fun volunteerHasRole(volunteerId: Int, vararg roleNames: RoleName) : Boolean {
        val role = volunteerRepository.getRole(volunteerId) ?: return false
        return roleNames.contains(role)
    }
}