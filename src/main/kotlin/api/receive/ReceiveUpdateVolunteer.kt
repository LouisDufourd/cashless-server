package fr.plaglefleau.api.receive

import fr.plaglefleau.database.dto.CivilityDTO
import fr.plaglefleau.database.tables.RoleName

data class ReceiveUpdateVolunteer(
    val id: Int,
    val username: String? = null,
    val password: String? = null,
    val role: RoleName? = null,
    var civilityId: Int? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val age: Int? = null,
    val standId: Int? = null
)